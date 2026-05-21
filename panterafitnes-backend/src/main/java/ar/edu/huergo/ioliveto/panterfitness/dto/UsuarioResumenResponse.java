package ar.edu.huergo.ioliveto.panterfitness.dto;

import ar.edu.huergo.ioliveto.panterfitness.enums.Rol;

public record UsuarioResumenResponse(
		Long id,
		String nombre,
		String apellido,
		String email,
		Rol rol
) {
}
