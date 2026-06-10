package com.sistema.panterafitness.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record QrSimuladoRequest(
		@NotBlank String qrSimulado,
		Long horarioClaseId,
		LocalDateTime horaIngresoSimulada
) {
}
