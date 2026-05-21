package ar.edu.huergo.ioliveto.panterfitness.dto;

import ar.edu.huergo.ioliveto.panterfitness.enums.Sector;

public record OcupacionSectorResponse(
		Long id,
		Sector sector,
		Integer ocupacionActual,
		Integer capacidadMaxima,
		Double porcentajeOcupacion,
		String estado
) {
}
