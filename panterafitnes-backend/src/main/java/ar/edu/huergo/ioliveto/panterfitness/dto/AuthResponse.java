package ar.edu.huergo.ioliveto.panterfitness.dto;

public record AuthResponse(
		String token,
		String tipo,
		UsuarioResponse usuario
) {
}
