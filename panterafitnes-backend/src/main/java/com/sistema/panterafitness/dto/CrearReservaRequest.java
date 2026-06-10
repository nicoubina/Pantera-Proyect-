package com.sistema.panterafitness.dto;

import jakarta.validation.constraints.NotNull;

public record CrearReservaRequest(
		@NotNull Long horarioClaseId
) {
}
