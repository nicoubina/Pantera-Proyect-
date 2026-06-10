package com.sistema.panterafitness.dto;

public record CancelarReservaResponse(
		String mensaje,
		ReservaResponse reserva,
		ReservaResponse reservaPromovida
) {
}
