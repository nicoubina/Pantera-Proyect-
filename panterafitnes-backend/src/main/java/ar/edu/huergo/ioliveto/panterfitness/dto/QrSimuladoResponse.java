package ar.edu.huergo.ioliveto.panterfitness.dto;

import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoAsistencia;
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
