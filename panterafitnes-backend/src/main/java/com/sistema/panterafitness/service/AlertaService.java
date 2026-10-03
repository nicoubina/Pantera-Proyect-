package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.entity.*;
import com.sistema.panterafitness.enums.*;
import com.sistema.panterafitness.exception.ResourceNotFoundException;
import com.sistema.panterafitness.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertaService {
  private final AlertaRepository repository;
  private final UsuarioRepository usuarios;
  private final UsuarioService usuarioService;
  private final NotificacionService notificaciones;

  @Transactional(readOnly = true)
  public List<AlertaResponse> listar() {
    var list =
        usuarioService.obtenerUsuarioAutenticado().getRol() == Rol.ADMINISTRADOR
            ? repository.findAll()
            : repository.findByActivaTrueOrderByFechaCreacionDesc();
    return list.stream()
        .sorted(
            Comparator.comparing(Alerta::getPrioridad)
                .reversed()
                .thenComparing(Alerta::getFechaCreacion, Comparator.reverseOrder()))
        .map(this::dto)
        .toList();
  }

  @Transactional
  public AlertaResponse guardar(Long id, AlertaRequest r) {
    Alerta a =
        id == null
            ? Alerta.builder().build()
            : repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada."));
    boolean anunciar = r.activa() && (id == null || !Boolean.TRUE.equals(a.getActiva()));
    a.setTitulo(r.titulo().trim());
    a.setDescripcion(r.descripcion());
    a.setActiva(r.activa());
    a.setPrioridad(r.prioridad());
    repository.save(a);
    if (anunciar)
      usuarios.findAll().stream()
          .filter(u -> Boolean.TRUE.equals(u.getActivo()))
          .forEach(
              u ->
                  notificaciones.crear(
                      u, a.getTitulo(), a.getDescripcion(), TipoNotificacion.ALERTA));
    return dto(a);
  }

  private AlertaResponse dto(Alerta a) {
    return new AlertaResponse(
        a.getId(),
        a.getTitulo(),
        a.getDescripcion(),
        a.getFechaCreacion(),
        a.getActiva(),
        a.getPrioridad());
  }
}
