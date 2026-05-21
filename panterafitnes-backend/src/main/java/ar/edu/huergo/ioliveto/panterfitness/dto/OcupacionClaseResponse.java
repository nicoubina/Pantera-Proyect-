package ar.edu.huergo.ioliveto.panterfitness.dto;

import ar.edu.huergo.ioliveto.panterfitness.enums.DiaSemana;
import java.time.LocalDate;
import java.time.LocalTime;

public record OcupacionClaseResponse(
		Long horarioId,
		Long claseId,
		String nombreClase,
		DiaSemana diaSemana,
		LocalDate fecha,
		LocalTime horaInicio,
		LocalTime horaFin,
		Integer ocupacionActual,
		Integer capacidadMaxima,
		Double porcentajeOcupacion,
		String estado
) {
}
