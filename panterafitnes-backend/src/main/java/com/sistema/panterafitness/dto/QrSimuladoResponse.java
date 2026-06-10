package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.EstadoAsistencia;
import java.time.LocalDateTime;

public record QrSimuladoResponse(
		String mensaje,
		Long reservaId,
		Long horarioClaseId,
		EstadoAsistencia estadoAsistencia,
		LocalDateTime horaIngreso,
		NotificacionResponse notificacion
) {
}
