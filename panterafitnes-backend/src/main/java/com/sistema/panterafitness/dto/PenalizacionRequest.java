package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record PenalizacionRequest(
    @NotNull Long usuarioId,
    @NotBlank @Size(max = 1000) String motivo,
    @NotNull LocalDate fechaInicio,
    @NotNull LocalDate fechaFin) {}
