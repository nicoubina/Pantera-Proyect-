package ar.edu.huergo.ioliveto.panterfitness.dto;

import ar.edu.huergo.ioliveto.panterfitness.enums.Sector;

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
