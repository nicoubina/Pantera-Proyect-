package ar.edu.huergo.ioliveto.panterfitness.dto;

import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoMembresia;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ActualizarMembresiaRequest(
		@NotNull EstadoMembresia estadoMembresia,
		LocalDate fechaInicioMembresia,
		LocalDate fechaVencimientoMembresia,
		Boolean activo
) {
}
