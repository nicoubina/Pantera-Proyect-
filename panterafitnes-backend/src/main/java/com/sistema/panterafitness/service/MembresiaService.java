package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.MembresiaResponse;
import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.*;
import com.sistema.panterafitness.exception.*;
import com.sistema.panterafitness.repository.UsuarioRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembresiaService {
  private final UsuarioService usuarios;
  private final UsuarioRepository repository;

  public static EstadoMembresia estadoActual(Usuario u) {
    if (u.getEstadoMembresia() != EstadoMembresia.ACTIVA) return u.getEstadoMembresia();
    LocalDate hoy = LocalDate.now();
    if (u.getFechaVencimientoMembresia() != null && u.getFechaVencimientoMembresia().isBefore(hoy))
      return EstadoMembresia.VENCIDA;
    if (u.getFechaInicioMembresia() != null && u.getFechaInicioMembresia().isAfter(hoy))
      return EstadoMembresia.PENDIENTE;
    return EstadoMembresia.ACTIVA;
  }

  public void validarReserva(Usuario u) {
    if (!Boolean.TRUE.equals(u.getActivo()) || estadoActual(u) != EstadoMembresia.ACTIVA)
      throw new BusinessException("La membresia debe estar ACTIVA y vigente para reservar.");
  }

  @Transactional(readOnly = true)
  public MembresiaResponse consultar(Long id) {
    Usuario actual = usuarios.obtenerUsuarioAutenticado();
    if (id != null && !actual.getId().equals(id) && actual.getRol() != Rol.ADMINISTRADOR)
      throw new ForbiddenException("Solo podes consultar tu membresia.");
    Usuario u =
        id == null
            ? actual
            : repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    return new MembresiaResponse(
        u.getId(), estadoActual(u), u.getFechaInicioMembresia(), u.getFechaVencimientoMembresia());
  }
}
