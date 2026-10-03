package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.entity.*;
import com.sistema.panterafitness.enums.*;
import com.sistema.panterafitness.exception.*;
import com.sistema.panterafitness.mapper.EntityMapper;
import com.sistema.panterafitness.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RutinaService {
  private final RutinaRepository repository;
  private final EjercicioRepository ejercicios;
  private final UsuarioRepository usuarios;
  private final UsuarioService usuarioService;
  private final ReservaRepository reservas;
  private final NotificacionService notificaciones;

  @Transactional(readOnly = true)
  public List<UsuarioResumenResponse> clientes() {
    Usuario u = usuarioService.obtenerUsuarioAutenticado();
    if (u.getRol() != Rol.PROFESOR && u.getRol() != Rol.ADMINISTRADOR)
      throw new ForbiddenException("No autorizado.");
    Set<Long> relacionados = new HashSet<>();
    if (u.getRol() == Rol.PROFESOR) {
      reservas.findByProfesorId(u.getId()).forEach(r -> relacionados.add(r.getUsuario().getId()));
      repository
          .findByProfesorIdOrderByIdDesc(u.getId())
          .forEach(r -> relacionados.add(r.getCliente().getId()));
    }
    return usuarios.findAll().stream()
        .filter(c -> c.getRol() == Rol.CLIENTE && Boolean.TRUE.equals(c.getActivo()))
        .filter(c -> u.getRol() == Rol.ADMINISTRADOR || relacionados.contains(c.getId()))
        .map(EntityMapper::toUsuarioResumenResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<RutinaResponse> listar() {
    Usuario u = usuarioService.obtenerUsuarioAutenticado();
    return (u.getRol() == Rol.ADMINISTRADOR
            ? repository.findAll()
            : u.getRol() == Rol.PROFESOR
                ? repository.findByProfesorIdOrderByIdDesc(u.getId())
                : repository.findByClienteIdOrderByIdDesc(u.getId()))
        .stream().map(this::dto).toList();
  }

  @Transactional(readOnly = true)
  public RutinaResponse obtener(Long id) {
    return dto(visible(id));
  }

  @Transactional
  public RutinaResponse guardar(Long id, RutinaRequest request) {
    Usuario u = usuarioService.obtenerUsuarioAutenticado();
    if (u.getRol() != Rol.PROFESOR)
      throw new ForbiddenException("Solo profesores pueden crear o editar rutinas propias.");
    if (clientes().stream().noneMatch(c -> c.id().equals(request.clienteId())))
      throw new ForbiddenException("El cliente debe estar relacionado con tus clases o rutinas.");
    Rutina rutina = id == null ? Rutina.builder().profesor(u).build() : visible(id);
    if (!rutina.getProfesor().getId().equals(u.getId()))
      throw new ForbiddenException("Solo podes editar tus rutinas.");
    rutina.setNombre(request.nombre().trim());
    rutina.setDescripcion(request.descripcion());
    rutina.setEstado(request.estado());
    rutina.setCliente(
        usuarios
            .findById(request.clienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado.")));
    rutina.getEjercicios().clear();
    int orden = 0;
    for (var e : request.ejercicios()) {
      Ejercicio ejercicio =
          ejercicios
              .findById(e.ejercicioId())
              .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado."));
      rutina
          .getEjercicios()
          .add(
              RutinaEjercicio.builder()
                  .rutina(rutina)
                  .ejercicio(ejercicio)
                  .series(e.series())
                  .repeticiones(e.repeticiones())
                  .pesoSugerido(e.pesoSugerido())
                  .descanso(e.descanso())
                  .orden(++orden)
                  .build());
    }
    repository.save(rutina);
    notificaciones.crear(
        rutina.getCliente(),
        id == null ? "Nueva rutina asignada" : "Rutina actualizada",
        rutina.getNombre(),
        TipoNotificacion.RUTINA);
    return dto(rutina);
  }

  @Transactional(readOnly = true)
  public List<EjercicioResponse> ejercicios() {
    return ejercicios.findAll().stream()
        .map(e -> new EjercicioResponse(e.getId(), e.getNombre(), e.getDescripcion()))
        .toList();
  }

  @Transactional
  public EjercicioResponse crearEjercicio(EjercicioRequest request) {
    if (ejercicios.existsByNombreIgnoreCase(request.nombre().trim()))
      throw new BusinessException("Ya existe un ejercicio con ese nombre.");
    Ejercicio e =
        ejercicios.save(
            Ejercicio.builder()
                .nombre(request.nombre().trim())
                .descripcion(request.descripcion())
                .build());
    return new EjercicioResponse(e.getId(), e.getNombre(), e.getDescripcion());
  }

  private Rutina visible(Long id) {
    Rutina r =
        repository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada."));
    Usuario u = usuarioService.obtenerUsuarioAutenticado();
    if (u.getRol() != Rol.ADMINISTRADOR
        && !(u.getRol() == Rol.CLIENTE ? r.getCliente().getId() : r.getProfesor().getId())
            .equals(u.getId())) throw new ForbiddenException("No tenes acceso a esta rutina.");
    return r;
  }

  private RutinaResponse dto(Rutina r) {
    return new RutinaResponse(
        r.getId(),
        r.getNombre(),
        r.getDescripcion(),
        EntityMapper.toUsuarioResumenResponse(r.getProfesor()),
        EntityMapper.toUsuarioResumenResponse(r.getCliente()),
        r.getEstado(),
        r.getEjercicios().stream()
            .map(
                e ->
                    new RutinaEjercicioResponse(
                        e.getId(),
                        new EjercicioResponse(
                            e.getEjercicio().getId(),
                            e.getEjercicio().getNombre(),
                            e.getEjercicio().getDescripcion()),
                        e.getSeries(),
                        e.getRepeticiones(),
                        e.getPesoSugerido(),
                        e.getDescanso(),
                        e.getOrden()))
            .toList());
  }
}
