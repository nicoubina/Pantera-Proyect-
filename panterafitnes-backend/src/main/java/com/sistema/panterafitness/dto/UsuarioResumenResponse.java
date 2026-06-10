package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.Rol;

public record UsuarioResumenResponse(
		Long id,
		String nombre,
		String apellido,
		String email,
		Rol rol
) {
}
