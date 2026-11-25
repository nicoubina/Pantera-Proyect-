package com.sistema.panterafitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Lectura de un QR en la entrada del gimnasio.
 *
 * @param codigo contenido del QR escaneado (codigo personal del cliente)
 * @param horarioClaseId opcional: clase concreta; si falta se usa la reserva confirmada mas cercana
 */
public record QrIngresoRequest(@NotBlank @Size(max = 120) String codigo, Long horarioClaseId) {}
