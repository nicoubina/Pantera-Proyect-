package ar.edu.huergo.ioliveto.panterfitness.dto;

public record CancelarReservaResponse(
		String mensaje,
		ReservaResponse reserva,
		ReservaResponse reservaPromovida
) {
}
