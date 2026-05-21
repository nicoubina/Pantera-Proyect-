package ar.edu.huergo.ioliveto.panterfitness.dto;

import java.util.List;

public record OcupacionGeneralResponse(
		Integer ocupacionActual,
		Integer capacidadMaxima,
		Double porcentajeOcupacion,
		String estado,
		List<OcupacionSectorResponse> sectores
) {
}
