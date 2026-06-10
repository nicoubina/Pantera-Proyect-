package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.DiaSemana;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioClaseRequest(
		@NotNull Long claseGimnasioId,
		DiaSemana diaSemana,
		@NotNull @FutureOrPresent LocalDate fecha,
		@NotNull LocalTime horaInicio,
		@NotNull LocalTime horaFin,
		@Min(1) Integer cupoMaximo,
		Boolean activa
) {
}
