package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.DiaSemana;
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
