package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.entity.*;
import com.sistema.panterafitness.enums.*;
import com.sistema.panterafitness.exception.*;
import com.sistema.panterafitness.mapper.EntityMapper;
import com.sistema.panterafitness.repository.*;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AsistenciaService {
  private final AsistenciaRepository repository;
  private final ReservaRepository reservas;
  private final UsuarioRepository usuarios;
  private final AccesoService acceso;
  private final PenalizacionService penalizaciones;
  private final SectorGimnasioRepository sectores;

  @Transactional(readOnly = true)
  public List<AsistenciaResponse> listar(Long usuarioId, Long horarioId, EstadoAsistencia estado) {
    return repository.findAll().stream()
        .filter(a -> acceso.visible(a.getHorarioClase(), a.getUsuario().getId()))
        .filter(a -> usuarioId == null || a.getUsuario().getId().equals(usuarioId))
        .filter(a -> horarioId == null || a.getHorarioClase().getId().equals(horarioId))
        .filter(a -> estado == null || a.getEstadoAsistencia() == estado)
        .sorted(Comparator.comparing(Asistencia::getFecha).reversed())
        .map(this::dto)
        .toList();
  }

  @Transactional(readOnly = true)
  public AsistenciaResponse obtener(Long id) {
    Asistencia a =
        repository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada."));
    acceso.validarHorario(a.getHorarioClase(), a.getUsuario().getId());
    return dto(a);
  }

  @Transactional
  public AsistenciaResponse registrar(RegistrarAsistenciaRequest r) {
    Reserva reserva =
        reservas
            .findLockedById(r.reservaId())
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada."));
    acceso.validarHorario(reserva.getHorarioClase(), reserva.getUsuario().getId());
    if (reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA)
      throw new BusinessException("Solo se registra asistencia de reservas confirmadas.");
    if (r.estadoAsistencia() != EstadoAsistencia.ASISTIDA
        && r.estadoAsistencia() != EstadoAsistencia.AUSENTE)
      throw new BusinessException("Selecciona ASISTIDA o AUSENTE.");
    if (reserva
        .getHorarioClase()
        .getFecha()
        .atTime(reserva.getHorarioClase().getHoraInicio())
        .isAfter(LocalDateTime.now()))
      throw new BusinessException(
          "La clase todavia no comenzo. Usa el QR para simular el ingreso.");
    return dto(
        registrarReserva(
            reserva, r.estadoAsistencia(), LocalDateTime.now(), MetodoRegistro.MANUAL));
  }

  @Transactional
  public Asistencia registrarReserva(
      Reserva r, EstadoAsistencia estado, LocalDateTime hora, MetodoRegistro metodo) {
    usuarios.findLockedById(r.getUsuario().getId()).orElseThrow();
    Asistencia existente = repository.findByReservaId(r.getId()).orElse(null);
    if (existente != null && existente.getEstadoAsistencia() != EstadoAsistencia.PENDIENTE)
      throw new BusinessException("La reserva ya tiene asistencia registrada.");
    HorarioClase h = r.getHorarioClase();
    Asistencia a =
        existente == null
            ? Asistencia.builder()
                .usuario(r.getUsuario())
                .reserva(r)
                .horarioClase(h)
                .fecha(h.getFecha())
                .horaProgramada(h.getHoraInicio())
                .build()
            : existente;
    a.setHoraIngreso(hora);
    a.setEstadoAsistencia(estado);
    a.setMetodoRegistro(metodo);
    repository.saveAndFlush(a);
    if (estado == EstadoAsistencia.ASISTIDA) {
      r.setEstadoReserva(EstadoReserva.ASISTIDA);
      var sector =
          sectores
              .findLockedByNombre(h.getClaseGimnasio().getSector())
              .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado."));
      if (sector.getOcupacionActual() < sector.getCapacidadMaxima())
        sector.setOcupacionActual(sector.getOcupacionActual() + 1);
    }
    if (estado == EstadoAsistencia.AUSENTE) {
      r.setEstadoReserva(EstadoReserva.AUSENTE);
      penalizaciones.evaluarFaltas(r.getUsuario());
    }
    return a;
  }

  @Transactional
  public void pendiente(Reserva r) {
    if (!repository.existsByReservaId(r.getId()))
      repository.save(
          Asistencia.builder()
              .usuario(r.getUsuario())
              .reserva(r)
              .horarioClase(r.getHorarioClase())
              .fecha(r.getHorarioClase().getFecha())
              .horaProgramada(r.getHorarioClase().getHoraInicio())
              .estadoAsistencia(EstadoAsistencia.PENDIENTE)
              .metodoRegistro(MetodoRegistro.AUTOMATICO)
              .build());
  }

  @Transactional
  public void cancelar(Reserva r) {
    if (!repository.existsByReservaId(r.getId())) pendiente(r);
    repository
        .findByReservaId(r.getId())
        .ifPresent(a -> a.setEstadoAsistencia(EstadoAsistencia.CANCELADA));
  }

  private AsistenciaResponse dto(Asistencia a) {
    return new AsistenciaResponse(
        a.getId(),
        EntityMapper.toUsuarioResumenResponse(a.getUsuario()),
        a.getReserva() == null ? null : a.getReserva().getId(),
        EntityMapper.toHorarioClaseResponse(a.getHorarioClase()),
        a.getFecha(),
        a.getHoraProgramada(),
        a.getHoraIngreso(),
        a.getEstadoAsistencia(),
        a.getMetodoRegistro());
  }
}
