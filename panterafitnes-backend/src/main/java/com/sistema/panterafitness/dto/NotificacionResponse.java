package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.EstadoNotificacion;
import com.sistema.panterafitness.enums.TipoNotificacion;
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
