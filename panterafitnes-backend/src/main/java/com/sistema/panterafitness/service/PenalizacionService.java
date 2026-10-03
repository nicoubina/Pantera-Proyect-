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
public class PenalizacionService {
  private final PenalizacionRepository repository;
  private final AsistenciaRepository asistencias;
  private final UsuarioRepository usuarios;
  private final UsuarioService usuarioService;
  private final NotificacionService notificaciones;

  public EstadoPenalizacion estadoActual(Penalizacion p) {
    return p.getEstado() == EstadoPenalizacion.ACTIVA && p.getFechaFin().isBefore(LocalDate.now())
        ? EstadoPenalizacion.FINALIZADA
        : p.getEstado();
  }

  @Transactional(readOnly = true)
  public boolean tieneBloqueo(Usuario u) {
    return repository
        .existsByUsuarioIdAndEstadoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            u.getId(), EstadoPenalizacion.ACTIVA, LocalDate.now(), LocalDate.now());
  }

  // Ciclos trimestrales individuales desde la primera falta: cada tercer evento sanciona una vez.
  @Transactional
  public void evaluarFaltas(Usuario u) {
    var faltas =
        asistencias.findByUsuarioIdAndEstadoAsistenciaOrderByFechaAscIdAsc(
            u.getId(), EstadoAsistencia.AUSENTE);
    LocalDate ciclo = null;
    int contador = 0;
    for (Asistencia a : faltas) {
      if (ciclo == null || !a.getFecha().isBefore(ciclo.plusMonths(3))) {
        ciclo = a.getFecha();
        contador = 0;
      }
      contador++;
      if (contador % 3 == 0
          && ciclo.plusMonths(3).isAfter(LocalDate.now())
          && !Boolean.TRUE.equals(a.getPenalizacionProcesada())) {
        usuarios.findLockedById(u.getId()).orElseThrow();
        long anteriores = repository.countByUsuarioId(u.getId());
        LocalDate inicio = LocalDate.now();
        repository.save(
            Penalizacion.builder()
                .usuario(u)
                .motivo("Tres ausencias en el ciclo trimestral")
                .fechaInicio(inicio)
                .fechaFin(inicio.plusMonths(anteriores == 0 ? 1 : 3).minusDays(1))
                .estado(EstadoPenalizacion.ACTIVA)
                .build());
        a.setPenalizacionProcesada(true);
        notificaciones.crear(
            u,
            "Penalizacion de reservas",
            "Se bloqueo la reserva anticipada por inasistencias.",
            TipoNotificacion.PENALIZACION);
      }
    }
  }

  @Transactional(readOnly = true)
  public List<PenalizacionResponse> listar() {
    Usuario u = usuarioService.obtenerUsuarioAutenticado();
    return (u.getRol() == Rol.ADMINISTRADOR
            ? repository.findAll()
            : repository.findByUsuarioIdOrderByFechaInicioDesc(u.getId()))
        .stream().map(this::dto).toList();
  }

  @Transactional(readOnly = true)
  public PenalizacionResponse obtener(Long id) {
    return dto(visible(id));
  }

  @Transactional
  public PenalizacionResponse crear(PenalizacionRequest r) {
    if (r.fechaFin().isBefore(r.fechaInicio()))
      throw new BusinessException("El vencimiento debe ser posterior al inicio.");
    Usuario u =
        usuarios
            .findLockedById(r.usuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    if (u.getRol() != Rol.CLIENTE) throw new BusinessException("Solo se penalizan clientes.");
    Penalizacion p =
        repository.save(
            Penalizacion.builder()
                .usuario(u)
                .motivo(r.motivo().trim())
                .fechaInicio(r.fechaInicio())
                .fechaFin(r.fechaFin())
                .estado(EstadoPenalizacion.ACTIVA)
                .build());
    notificaciones.crear(u, "Penalizacion registrada", r.motivo(), TipoNotificacion.PENALIZACION);
    return dto(p);
  }

  @Transactional
  public PenalizacionResponse cancelar(Long id) {
    Penalizacion p = visible(id);
    if (estadoActual(p) != EstadoPenalizacion.ACTIVA)
      throw new BusinessException("La penalizacion no esta activa.");
    p.setEstado(EstadoPenalizacion.CANCELADA);
    notificaciones.crear(
        p.getUsuario(), "Penalizacion cancelada", p.getMotivo(), TipoNotificacion.PENALIZACION);
    return dto(p);
  }

  private Penalizacion visible(Long id) {
    Penalizacion p =
        repository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Penalizacion no encontrada."));
    Usuario u = usuarioService.obtenerUsuarioAutenticado();
    if (u.getRol() != Rol.ADMINISTRADOR && !p.getUsuario().getId().equals(u.getId()))
      throw new ForbiddenException("Solo podes consultar tus penalizaciones.");
    return p;
  }

  private PenalizacionResponse dto(Penalizacion p) {
    return new PenalizacionResponse(
        p.getId(),
        EntityMapper.toUsuarioResumenResponse(p.getUsuario()),
        p.getMotivo(),
        p.getFechaInicio(),
        p.getFechaFin(),
        estadoActual(p));
  }
}
