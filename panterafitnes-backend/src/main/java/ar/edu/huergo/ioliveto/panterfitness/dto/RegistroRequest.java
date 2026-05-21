package ar.edu.huergo.ioliveto.panterfitness.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
		@NotBlank String nombre,
		String apellido,
		@NotBlank @Email String email,
		@NotBlank @Size(min = 6) String password
) {
}
