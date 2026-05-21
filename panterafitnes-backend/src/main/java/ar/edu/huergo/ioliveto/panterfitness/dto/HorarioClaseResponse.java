package ar.edu.huergo.ioliveto.panterfitness.dto;

import ar.edu.huergo.ioliveto.panterfitness.enums.DiaSemana;
import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioClaseResponse(
		Long id,
		ClaseResponse claseGimnasio,
		DiaSemana diaSemana,
		LocalDate fecha,
		LocalTime horaInicio,
		LocalTime horaFin,
		Integer cupoMaximo,
		Boolean activa
) {
}
