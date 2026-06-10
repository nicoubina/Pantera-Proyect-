package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.EstadoMembresia;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ActualizarMembresiaRequest(
		@NotNull EstadoMembresia estadoMembresia,
		LocalDate fechaInicioMembresia,
		LocalDate fechaVencimientoMembresia,
		Boolean activo
) {
}
