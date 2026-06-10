package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.Sector;

public record ClaseResponse(
		Long id,
		String nombre,
		String descripcion,
		UsuarioResumenResponse profesor,
		Sector sector,
		Integer cupoMaximo,
		Boolean activa
) {
}
