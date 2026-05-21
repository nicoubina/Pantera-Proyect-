package ar.edu.huergo.ioliveto.panterfitness.dto;

import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoReserva;
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
