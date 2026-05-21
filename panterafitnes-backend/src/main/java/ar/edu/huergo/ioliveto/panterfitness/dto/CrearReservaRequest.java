package ar.edu.huergo.ioliveto.panterfitness.dto;

import jakarta.validation.constraints.NotNull;

public record CrearReservaRequest(
		@NotNull Long horarioClaseId
) {
}
