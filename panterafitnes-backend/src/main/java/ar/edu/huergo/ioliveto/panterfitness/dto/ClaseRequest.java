package ar.edu.huergo.ioliveto.panterfitness.dto;

import ar.edu.huergo.ioliveto.panterfitness.enums.Sector;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClaseRequest(
		@NotBlank String nombre,
		String descripcion,
		@NotNull Long profesorId,
		@NotNull Sector sector,
		@Min(1) Integer cupoMaximo,
		Boolean activa
) {
}
