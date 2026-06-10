package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.EstadoReserva;
import java.time.LocalDateTime;

public record ReservaResponse(
		Long id,
		UsuarioResumenResponse usuario,
		HorarioClaseResponse horarioClase,
		EstadoReserva estadoReserva,
		LocalDateTime fechaCreacion,
		LocalDateTime fechaCancelacion,
		Integer posicionListaEspera
) {
}
