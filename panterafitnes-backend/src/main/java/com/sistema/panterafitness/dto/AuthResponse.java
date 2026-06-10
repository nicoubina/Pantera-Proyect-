package com.sistema.panterafitness.dto;

public record AuthResponse(
		String token,
		String tipo,
		UsuarioResponse usuario
) {
}
