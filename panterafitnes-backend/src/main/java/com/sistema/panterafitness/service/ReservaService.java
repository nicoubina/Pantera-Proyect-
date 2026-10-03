package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.CancelarReservaResponse;
import com.sistema.panterafitness.dto.CrearReservaRequest;
import com.sistema.panterafitness.dto.ReservaResponse;
import com.sistema.panterafitness.entity.HorarioClase;
import com.sistema.panterafitness.entity.ListaEspera;
import com.sistema.panterafitness.entity.Reserva;
import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.EstadoMembresia;
import com.sistema.panterafitness.enums.EstadoReserva;
import com.sistema.panterafitness.enums.Rol;
import com.sistema.panterafitness.enums.TipoNotificacion;
import com.sistema.panterafitness.exception.BusinessException;
import com.sistema.panterafitness.exception.ForbiddenException;
import com.sistema.panterafitness.exception.ResourceNotFoundException;
import com.sistema.panterafitness.mapper.EntityMapper;
import com.sistema.panterafitness.repository.HorarioClaseRepository;
import com.sistema.panterafitness.repository.ListaEsperaRepository;
import com.sistema.panterafitness.repository.ReservaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservaService {

  private static final List<EstadoReserva> ESTADOS_RESERVA_ACTIVA =
      List.of(
          EstadoReserva.CONFIRMADA,
          EstadoReserva.EN_ESPERA,
          EstadoReserva.ASISTIDA,
          EstadoReserva.AUSENTE);

  private static final List<EstadoReserva> ESTADOS_SUPERPONIBLES =
      List.of(EstadoReserva.CONFIRMADA, EstadoReserva.EN_ESPERA);

  private static final List<EstadoReserva> ESTADOS_OCUPAN_CUPO =
      List.of(EstadoReserva.CONFIRMADA, EstadoReserva.ASISTIDA);

  private final ReservaRepository reservaRepository;
  private final HorarioClaseRepository horarioClaseRepository;
  private final ListaEsperaRepository listaEsperaRepository;
  private final UsuarioService usuarioService;
  private final NotificacionService notificacionService;
  private final AsistenciaService asistenciaService;
  private final MembresiaService membresiaService;
  private final PenalizacionService penalizacionService;

  @Transactional
  public ReservaResponse crearReserva(CrearReservaRequest request) {
    Usuario usuario = usuarioService.obtenerUsuarioAutenticado();
    validarPuedeReservar(usuario);

    HorarioClase horario =
        horarioClaseRepository
            .findLockedById(request.horarioClaseId())
            .orElseThrow(() -> new ResourceNotFoundException("Horario de clase no encontrado."));
    if (!Boolean.TRUE.equals(horario.getActiva())
        || !Boolean.TRUE.equals(horario.getClaseGimnasio().getActiva())) {
      throw new BusinessException("El horario de clase no esta activo.");
    }

    boolean enElMomento =
        !inicio(horario).isAfter(LocalDateTime.now().plusMinutes(30))
            && !inicio(horario).isBefore(LocalDateTime.now());
    if (penalizacionService.tieneBloqueo(usuario) && !enElMomento)
      throw new BusinessException(
          "Tenes una penalizacion activa. Solo podes reservar en el momento, dentro de los 30"
              + " minutos previos, si hay cupo.");
    validarVentanaReserva(horario);
    if (enElMomento && !hayCupoDisponible(horario))
      throw new BusinessException("No hay cupo disponible para reservar en el momento.");
    validarReservaDuplicada(usuario, horario);
    validarSinSuperposicion(usuario, horario);

    EstadoReserva estado =
        hayCupoDisponible(horario) ? EstadoReserva.CONFIRMADA : EstadoReserva.EN_ESPERA;

    Reserva reserva =
        reservaRepository.save(
            Reserva.builder().usuario(usuario).horarioClase(horario).estadoReserva(estado).build());

    if (estado == EstadoReserva.EN_ESPERA) {
      int posicion =
          (int) listaEsperaRepository.countByHorarioClaseIdAndActivaTrue(horario.getId()) + 1;
      listaEsperaRepository.save(
          ListaEspera.builder()
              .usuario(usuario)
              .horarioClase(horario)
              .posicion(posicion)
              .activa(true)
              .build());
      notificacionService.crear(
          usuario,
          "Ingreso a lista de espera",
          "La clase "
              + horario.getClaseGimnasio().getNombre()
              + " esta completa. Quedaste en posicion "
              + posicion
              + ".",
          TipoNotificacion.LISTA_ESPERA);
    } else {
      asistenciaService.pendiente(reserva);
      notificacionService.crear(
          usuario,
          "Reserva confirmada",
          "Tu reserva para " + horario.getClaseGimnasio().getNombre() + " fue confirmada.",
          TipoNotificacion.RESERVA);
    }

    return toReservaResponse(reserva);
  }

  @Transactional(readOnly = true)
  public List<ReservaResponse> listarMisReservas() {
    Usuario usuario = usuarioService.obtenerUsuarioAutenticado();
    return reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId()).stream()
        .map(this::toReservaResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ReservaResponse> listarReservas() {
    Usuario usuario = usuarioService.obtenerUsuarioAutenticado();
    List<Reserva> reservas;
    if (usuario.getRol() == Rol.ADMINISTRADOR) {
      reservas = reservaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"));
    } else if (usuario.getRol() == Rol.PROFESOR) {
      reservas = reservaRepository.findByProfesorId(usuario.getId());
    } else {
      reservas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId());
    }
    return reservas.stream().map(this::toReservaResponse).toList();
  }

  @Transactional
  public CancelarReservaResponse cancelar(Long reservaId) {
    Usuario usuario = usuarioService.obtenerUsuarioAutenticado();
    Reserva reserva =
        reservaRepository
            .findLockedById(reservaId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada."));

    boolean esPropia = reserva.getUsuario().getId().equals(usuario.getId());
    boolean esAdmin = usuario.getRol() == Rol.ADMINISTRADOR;
    if (!esPropia && !esAdmin) {
      throw new ForbiddenException("No podes cancelar una reserva de otro usuario.");
    }
    if (reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
      throw new BusinessException("La reserva ya esta cancelada.");
    }

    if (reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA
        && reserva.getEstadoReserva() != EstadoReserva.EN_ESPERA)
      throw new BusinessException("Solo se cancelan reservas confirmadas o en espera.");
    horarioClaseRepository.findLockedById(reserva.getHorarioClase().getId()).orElseThrow();
    validarVentanaCancelacion(reserva.getHorarioClase());

    EstadoReserva estadoAnterior = reserva.getEstadoReserva();
    reserva.setEstadoReserva(EstadoReserva.CANCELADA);
    reserva.setFechaCancelacion(LocalDateTime.now());
    asistenciaService.cancelar(reserva);

    listaEsperaRepository
        .findByUsuarioIdAndHorarioClaseIdAndActivaTrue(
            reserva.getUsuario().getId(), reserva.getHorarioClase().getId())
        .ifPresent(lista -> lista.setActiva(false));

    notificacionService.crear(
        reserva.getUsuario(),
        "Reserva cancelada",
        "Tu reserva para "
            + reserva.getHorarioClase().getClaseGimnasio().getNombre()
            + " fue cancelada.",
        TipoNotificacion.CANCELACION);

    Reserva promovida = null;
    if (estadoAnterior == EstadoReserva.CONFIRMADA) {
      promovida = promoverPrimeroEnEspera(reserva.getHorarioClase());
    }
    recalcularPosiciones(reserva.getHorarioClase().getId());

    return new CancelarReservaResponse(
        "Cancelacion realizada.",
        toReservaResponse(reserva),
        promovida == null ? null : toReservaResponse(promovida));
  }

  private void validarPuedeReservar(Usuario usuario) {
    if (usuario.getRol() != Rol.CLIENTE) {
      throw new ForbiddenException("Solo usuarios con rol CLIENTE pueden reservar clases.");
    }
    membresiaService.validarReserva(usuario);
    if (usuario.getEstadoMembresia() != EstadoMembresia.ACTIVA) {
      throw new BusinessException("La membresia debe estar ACTIVA para reservar.");
    }
    if (!Boolean.TRUE.equals(usuario.getActivo())) {
      throw new BusinessException("El usuario no esta activo.");
    }
  }

  private void validarVentanaReserva(HorarioClase horario) {
    LocalDateTime ahora = LocalDateTime.now();
    LocalDateTime inicio = inicio(horario);
    if (inicio.isAfter(ahora.plusWeeks(1))) {
      throw new BusinessException(
          "La reserva puede hacerse como maximo con 1 semana de anticipacion.");
    }
    if (inicio.isBefore(ahora)) {
      throw new BusinessException("La clase ya comenzo.");
    }
  }

  private void validarVentanaCancelacion(HorarioClase horario) {
    LocalDateTime ahora = LocalDateTime.now();
    LocalDateTime inicio = inicio(horario);
    if (inicio.isBefore(ahora.plusHours(24))) {
      throw new BusinessException(
          "La cancelacion solo se permite hasta 24 horas antes del inicio de la clase.");
    }
  }

  private void validarReservaDuplicada(Usuario usuario, HorarioClase horario) {
    boolean existe =
        reservaRepository.existsByUsuarioIdAndHorarioClaseIdAndEstadoReservaIn(
            usuario.getId(), horario.getId(), ESTADOS_RESERVA_ACTIVA);
    if (existe) {
      throw new BusinessException(
          "El usuario ya tiene una reserva o lista de espera para ese horario.");
    }
  }

  private void validarSinSuperposicion(Usuario usuario, HorarioClase nuevoHorario) {
    List<Reserva> reservas =
        reservaRepository.findByUsuarioIdAndEstadoReservaIn(usuario.getId(), ESTADOS_SUPERPONIBLES);
    for (Reserva reserva : reservas) {
      HorarioClase reservado = reserva.getHorarioClase();
      if (reservado.getId().equals(nuevoHorario.getId())) {
        continue;
      }
      if (seSuperponen(reservado, nuevoHorario)) {
        throw new BusinessException("El usuario no puede reservar dos clases superpuestas.");
      }
    }
  }

  private boolean hayCupoDisponible(HorarioClase horario) {
    long ocupados =
        reservaRepository.countByHorarioClaseIdAndEstadoReservaIn(
            horario.getId(), ESTADOS_OCUPAN_CUPO);
    return ocupados < horario.getCupoMaximo();
  }

  private Reserva promoverPrimeroEnEspera(HorarioClase horario) {
    List<ListaEspera> esperaActiva =
        listaEsperaRepository.findByHorarioClaseIdAndActivaTrueOrderByFechaIngresoAsc(
            horario.getId());

    for (ListaEspera lista : esperaActiva) {
      Optional<Reserva> reservaEnEspera =
          reservaRepository.findByUsuarioIdAndHorarioClaseIdAndEstadoReserva(
              lista.getUsuario().getId(), horario.getId(), EstadoReserva.EN_ESPERA);
      lista.setActiva(false);
      if (reservaEnEspera.isPresent()) {
        Usuario candidato = lista.getUsuario();
        boolean enMomento = !inicio(horario).isAfter(LocalDateTime.now().plusMinutes(30));
        if (!Boolean.TRUE.equals(candidato.getActivo())
            || membresiaService.estadoActual(candidato) != EstadoMembresia.ACTIVA
            || (penalizacionService.tieneBloqueo(candidato) && !enMomento)) {
          reservaEnEspera.get().setEstadoReserva(EstadoReserva.CANCELADA);
          reservaEnEspera.get().setFechaCancelacion(LocalDateTime.now());
          asistenciaService.cancelar(reservaEnEspera.get());
          notificacionService.crear(
              candidato,
              "Reserva en espera cancelada",
              "No se pudo asignar el cupo por membresia o penalizacion.",
              TipoNotificacion.LISTA_ESPERA);
          continue;
        }
        Reserva reserva = reservaEnEspera.get();
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        asistenciaService.pendiente(reserva);
        notificacionService.crear(
            reserva.getUsuario(),
            "Cupo asignado",
            "Se libero un cupo y tu reserva para "
                + horario.getClaseGimnasio().getNombre()
                + " fue confirmada.",
            TipoNotificacion.LISTA_ESPERA);
        return reserva;
      }
    }
    return null;
  }

  private void recalcularPosiciones(Long horarioId) {
    List<ListaEspera> esperaActiva =
        listaEsperaRepository.findByHorarioClaseIdAndActivaTrueOrderByFechaIngresoAsc(horarioId);
    for (int i = 0; i < esperaActiva.size(); i++) {
      esperaActiva.get(i).setPosicion(i + 1);
    }
  }

  private ReservaResponse toReservaResponse(Reserva reserva) {
    Integer posicion = null;
    if (reserva.getEstadoReserva() == EstadoReserva.EN_ESPERA) {
      posicion =
          listaEsperaRepository
              .findByUsuarioIdAndHorarioClaseIdAndActivaTrue(
                  reserva.getUsuario().getId(), reserva.getHorarioClase().getId())
              .map(ListaEspera::getPosicion)
              .orElse(null);
    }
    return EntityMapper.toReservaResponse(reserva, posicion);
  }

  private LocalDateTime inicio(HorarioClase horario) {
    return horario.getFecha().atTime(horario.getHoraInicio());
  }

  private LocalDateTime fin(HorarioClase horario) {
    return horario.getFecha().atTime(horario.getHoraFin());
  }

  private boolean seSuperponen(HorarioClase primero, HorarioClase segundo) {
    return inicio(primero).isBefore(fin(segundo)) && inicio(segundo).isBefore(fin(primero));
  }
}
