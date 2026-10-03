package com.sistema.panterafitness.service;

import com.sistema.panterafitness.entity.*;
import com.sistema.panterafitness.enums.Rol;
import com.sistema.panterafitness.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccesoService {
  private final UsuarioService usuarios;

  public void validarHorario(HorarioClase horario, Long propietarioId) {
    Usuario u = usuarios.obtenerUsuarioAutenticado();
    if (u.getRol() == Rol.ADMINISTRADOR
        || (u.getRol() == Rol.CLIENTE && u.getId().equals(propietarioId))
        || (u.getRol() == Rol.PROFESOR
            && u.getId().equals(horario.getClaseGimnasio().getProfesor().getId()))) return;
    throw new ForbiddenException("No tenes acceso a esta clase o usuario.");
  }

  public boolean visible(HorarioClase horario, Long propietarioId) {
    Usuario u = usuarios.obtenerUsuarioAutenticado();
    return u.getRol() == Rol.ADMINISTRADOR
        || (u.getRol() == Rol.CLIENTE && u.getId().equals(propietarioId))
        || (u.getRol() == Rol.PROFESOR
            && u.getId().equals(horario.getClaseGimnasio().getProfesor().getId()));
  }
}
