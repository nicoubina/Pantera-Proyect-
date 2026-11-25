package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.QrIngresoRequest;
import com.sistema.panterafitness.dto.QrIngresoResponse;
import com.sistema.panterafitness.entity.HorarioClase;
import com.sistema.panterafitness.entity.Reserva;
import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.EstadoAsistencia;
import com.sistema.panterafitness.enums.EstadoMembresia;
import com.sistema.panterafitness.enums.EstadoReserva;
import com.sistema.panterafitness.enums.MetodoRegistro;
import com.sistema.panterafitness.enums.Rol;
import com.sistema.panterafitness.enums.TipoNotificacion;
import com.sistema.panterafitness.exception.BusinessException;
import com.sistema.panterafitness.repository.ReservaRepository;
import com.sistema.panterafitness.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra el ingreso de un socio cuando recepcion (administrador) o el profesor escanea su QR.
 * La hora de ingreso es siempre la del servidor: no se acepta una hora enviada por el cliente.
 */
@Service
@RequiredArgsConstructor
public class QrIngresoService {

  /** Se puede ingresar desde 30 minutos antes del inicio hasta 30 minutos despues del fin. */
  static final int MARGEN_MINUTOS = 30;

  /** Mas de 10 minutos tarde cuenta como ausente (regla de negocio de puntualidad). */
  static final int TOLERANCIA_MINUTOS = 10;

  private final UsuarioRepository usuarioRepository;
  private final ReservaRepository reservaRepository;
  private final AsistenciaService asistenciaService;
  private final AccesoService accesoService;
  private final NotificacionService notificacionService;

  @Transactional
  public QrIngresoResponse registrarIngreso(QrIngresoRequest request) {
    LocalDateTime ahora = LocalDateTime.now();
    Usuario socio =
        usuarioRepository
            .findByCodigoQr(request.codigo().trim())
            .orElseThrow(() -> new BusinessException("El QR no corresponde a ningún socio."));

    if (socio.getRol() != Rol.CLIENTE)
      throw new BusinessException("El QR no pertenece a un cliente del gimnasio.");
    if (!Boolean.TRUE.equals(socio.getActivo()))
      throw new BusinessException("El socio está inactivo.");
    if (MembresiaService.estadoActual(socio) != EstadoMembresia.ACTIVA)
      throw new BusinessException(
          "La membresía de " + nombre(socio) + " no está activa: no puede ingresar a clase.");

    Reserva reserva = resolverReserva(socio, request.horarioClaseId(), ahora);
    HorarioClase horario = reserva.getHorarioClase();
    accesoService.validarHorario(horario, socio.getId());
    reserva = reservaRepository.findLockedById(reserva.getId()).orElseThrow();
    if (reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA)
      throw new BusinessException("La reserva ya no está confirmada.");
    if (!esClaseCercana(horario, ahora))
      throw new BusinessException(
          "Fuera de horario: el ingreso se registra desde 30 minutos antes del inicio hasta 30"
              + " minutos después del fin de la clase.");

    EstadoAsistencia estado =
        esLlegadaTarde(horario, ahora) ? EstadoAsistencia.AUSENTE : EstadoAsistencia.ASISTIDA;
    reserva.setEstadoReserva(
        estado == EstadoAsistencia.ASISTIDA ? EstadoReserva.ASISTIDA : EstadoReserva.AUSENTE);
    asistenciaService.registrarReserva(reserva, estado, ahora, MetodoRegistro.QR);

    String clase = horario.getClaseGimnasio().getNombre();
    String mensaje =
        estado == EstadoAsistencia.ASISTIDA
            ? "Ingreso registrado: " + nombre(socio) + " a " + clase + "."
            : "Llegada tarde de " + nombre(socio) + " (más de 10 minutos): se registra como AUSENTE.";
    notificacionService.crear(
        socio,
        estado == EstadoAsistencia.ASISTIDA ? "Ingreso registrado" : "Llegada tarde",
        estado == EstadoAsistencia.ASISTIDA
            ? "Registramos tu ingreso a " + clase + ". ¡Buen entrenamiento!"
            : "Llegaste más de 10 minutos tarde a " + clase + ": la asistencia quedó como AUSENTE.",
        TipoNotificacion.QR);

    return new QrIngresoResponse(
        mensaje,
        socio.getId(),
        nombre(socio),
        reserva.getId(),
        horario.getId(),
        clase,
        estado,
        ahora);
  }

  private Reserva resolverReserva(Usuario socio, Long horarioClaseId, LocalDateTime ahora) {
    if (horarioClaseId != null) {
      return reservaRepository
          .findByUsuarioIdAndHorarioClaseIdAndEstadoReserva(
              socio.getId(), horarioClaseId, EstadoReserva.CONFIRMADA)
          .orElseThrow(
              () ->
                  new BusinessException(
                      nombre(socio) + " no tiene una reserva confirmada para esa clase."));
    }
    List<Reserva> candidatas =
        reservaRepository
            .findByUsuarioIdAndEstadoReservaIn(socio.getId(), List.of(EstadoReserva.CONFIRMADA))
            .stream()
            .filter(r -> esClaseCercana(r.getHorarioClase(), ahora))
            .filter(r -> accesoService.visible(r.getHorarioClase(), socio.getId()))
            .toList();
    return candidatas.stream()
        .min(Comparator.comparing(r -> inicio(r.getHorarioClase())))
        .orElseThrow(
            () ->
                new BusinessException(
                    nombre(socio)
                        + " no tiene una reserva confirmada para una clase de este horario."));
  }

  private static LocalDateTime inicio(HorarioClase h) {
    return h.getFecha().atTime(h.getHoraInicio());
  }

  static boolean esClaseCercana(HorarioClase h, LocalDateTime ahora) {
    LocalDateTime desde = inicio(h).minusMinutes(MARGEN_MINUTOS);
    LocalDateTime hasta = h.getFecha().atTime(h.getHoraFin()).plusMinutes(MARGEN_MINUTOS);
    return !ahora.isBefore(desde) && !ahora.isAfter(hasta);
  }

  static boolean esLlegadaTarde(HorarioClase h, LocalDateTime ahora) {
    return ahora.isAfter(inicio(h).plusMinutes(TOLERANCIA_MINUTOS));
  }

  private static String nombre(Usuario u) {
    return u.getNombre() + " " + u.getApellido();
  }
}
