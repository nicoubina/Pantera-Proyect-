package ar.edu.huergo.ioliveto.panterfitness.dto;

import ar.edu.huergo.ioliveto.panterfitness.enums.EstadoMembresia;
import ar.edu.huergo.ioliveto.panterfitness.enums.Rol;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioResponse(
		Long id,
		String nombre,
		String apellido,
		String email,
		Rol rol,
		EstadoMembresia estadoMembresia,
		LocalDate fechaInicioMembresia,
		LocalDate fechaVencimientoMembresia,
		Boolean activo,
		String qrSimulado,
		LocalDateTime fechaCreacion
) {
}
