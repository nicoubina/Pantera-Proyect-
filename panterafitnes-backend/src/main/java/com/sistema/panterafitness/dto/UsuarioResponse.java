package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.EstadoMembresia;
import com.sistema.panterafitness.enums.Rol;
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
