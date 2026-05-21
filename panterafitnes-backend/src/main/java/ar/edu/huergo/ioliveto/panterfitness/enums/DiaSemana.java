package ar.edu.huergo.ioliveto.panterfitness.enums;

import java.time.DayOfWeek;

public enum DiaSemana {
	LUNES,
	MARTES,
	MIERCOLES,
	JUEVES,
	VIERNES,
	SABADO,
	DOMINGO;

	public static DiaSemana from(DayOfWeek dayOfWeek) {
		return switch (dayOfWeek) {
			case MONDAY -> LUNES;
			case TUESDAY -> MARTES;
			case WEDNESDAY -> MIERCOLES;
			case THURSDAY -> JUEVES;
			case FRIDAY -> VIERNES;
			case SATURDAY -> SABADO;
			case SUNDAY -> DOMINGO;
		};
	}
}
