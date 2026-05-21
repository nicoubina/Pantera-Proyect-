package ar.edu.huergo.ioliveto.panterfitness.dto;

import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoNotificacion;
import ar.edu.huergo.ioliveto.panterfitness.enums.TipoNotificacion;
import java.time.LocalDateTime;

public record NotificacionResponse(
		Long id,
		String titulo,
		String mensaje,
		TipoNotificacion tipoNotificacion,
		EstadoNotificacion estadoNotificacion,
		LocalDateTime fechaCreacion
) {
}
