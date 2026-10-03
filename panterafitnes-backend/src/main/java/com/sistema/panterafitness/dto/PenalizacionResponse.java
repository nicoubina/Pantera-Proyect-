package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record PenalizacionResponse(
    Long id,
    UsuarioResumenResponse usuario,
    String motivo,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    EstadoPenalizacion estado) {}
