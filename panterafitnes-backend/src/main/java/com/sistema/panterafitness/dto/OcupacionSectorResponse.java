package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.Sector;

public record OcupacionSectorResponse(
		Long id,
		Sector sector,
		Integer ocupacionActual,
		Integer capacidadMaxima,
		Double porcentajeOcupacion,
		String estado
) {
}
