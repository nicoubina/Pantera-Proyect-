package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.ActualizarMembresiaRequest;
import com.sistema.panterafitness.dto.UsuarioResponse;
import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.exception.ResourceNotFoundException;
import com.sistema.panterafitness.exception.UnauthorizedException;
import com.sistema.panterafitness.mapper.EntityMapper;
import com.sistema.panterafitness.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
  private final com.sistema.panterafitness.repository.ClaseGimnasioRepository clases;
  private final com.sistema.panterafitness.repository.RutinaRepository rutinas;

  @Transactional(readOnly = true)
  public UsuarioResponse obtener(Long id) {
    return perfil(
        usuarioRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado.")));
  }

  @Transactional
  public UsuarioResponse guardar(Long id, com.sistema.panterafitness.dto.UsuarioAdminRequest r) {
    Usuario actual = obtenerUsuarioAutenticado();
    Usuario u =
        id == null
            ? Usuario.builder()
                .estadoMembresia(com.sistema.panterafitness.enums.EstadoMembresia.PENDIENTE)
                .qrSimulado("PF-" + java.util.UUID.randomUUID())
                .build()
            : usuarioRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    String email = r.email().trim().toLowerCase(java.util.Locale.ROOT);
    usuarioRepository
        .findByEmail(email)
        .filter(e -> !e.getId().equals(u.getId()))
        .ifPresent(
            e -> {
              throw new com.sistema.panterafitness.exception.BusinessException(
                  "Ya existe un usuario con ese email.");
            });
    if (id == null && (r.password() == null || r.password().isBlank()))
      throw new com.sistema.panterafitness.exception.BusinessException(
          "La contraseña es obligatoria.");
    if (actual.getId().equals(id)
        && (!r.activo() || r.rol() != com.sistema.panterafitness.enums.Rol.ADMINISTRADOR))
      throw new com.sistema.panterafitness.exception.BusinessException(
          "No podes desactivar tu propia cuenta ni quitarte el rol administrador.");
    if (id != null
        && u.getRol() != r.rol()
        && (clases.findAll().stream().anyMatch(c -> c.getProfesor().getId().equals(id))
            || !rutinas.findByProfesorIdOrderByIdDesc(id).isEmpty()
            || !rutinas.findByClienteIdOrderByIdDesc(id).isEmpty()))
      throw new com.sistema.panterafitness.exception.BusinessException(
          "El usuario tiene clases o rutinas relacionadas. Conserva su rol.");
    u.setNombre(r.nombre().trim());
    u.setApellido(r.apellido() == null ? "" : r.apellido().trim());
    u.setEmail(email);
    u.setRol(r.rol());
    u.setActivo(r.activo());
    if (r.password() != null && !r.password().isBlank())
      u.setPassword(passwordEncoder.encode(r.password()));
    return perfil(usuarioRepository.save(u));
  }

  private UsuarioResponse perfil(Usuario u) {
    var r = EntityMapper.toUsuarioResponse(u);
    return new UsuarioResponse(
        r.id(),
        r.nombre(),
        r.apellido(),
        r.email(),
        r.rol(),
        MembresiaService.estadoActual(u),
        r.fechaInicioMembresia(),
        r.fechaVencimientoMembresia(),
        r.activo(),
        r.qrSimulado(),
        r.fechaCreacion());
  }

  @Transactional(readOnly = true)
  public Usuario obtenerUsuarioAutenticado() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      throw new UnauthorizedException("No hay usuario autenticado.");
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof Usuario usuario) {
      return usuarioRepository
          .findById(usuario.getId())
          .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado."));
    }

    return usuarioRepository
        .findByEmail(authentication.getName())
        .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado."));
  }

  @Transactional(readOnly = true)
  public UsuarioResponse obtenerPerfil() {
    return perfil(obtenerUsuarioAutenticado());
  }

  @Transactional(readOnly = true)
  public List<UsuarioResponse> listarTodos() {
    return usuarioRepository.findAll().stream().map(this::perfil).toList();
  }

  @Transactional
  public UsuarioResponse actualizarMembresia(Long id, ActualizarMembresiaRequest request) {
    Usuario usuario =
        usuarioRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    if (request.fechaInicioMembresia() != null
        && request.fechaVencimientoMembresia() != null
        && request.fechaVencimientoMembresia().isBefore(request.fechaInicioMembresia()))
      throw new com.sistema.panterafitness.exception.BusinessException(
          "El vencimiento no puede ser anterior al inicio.");
    if (request.estadoMembresia() == com.sistema.panterafitness.enums.EstadoMembresia.ACTIVA
        && (request.fechaInicioMembresia() == null || request.fechaVencimientoMembresia() == null))
      throw new com.sistema.panterafitness.exception.BusinessException(
          "La membresia activa requiere ambas fechas.");
    if (obtenerUsuarioAutenticado().getId().equals(id) && Boolean.FALSE.equals(request.activo()))
      throw new com.sistema.panterafitness.exception.BusinessException(
          "No podes desactivar tu propia cuenta.");
    usuario.setEstadoMembresia(request.estadoMembresia());
    usuario.setFechaInicioMembresia(request.fechaInicioMembresia());
    usuario.setFechaVencimientoMembresia(request.fechaVencimientoMembresia());
    if (request.activo() != null) {
      usuario.setActivo(request.activo());
    }
    return perfil(usuario);
  }
}
