package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record AlertaRequest(
    @NotBlank @Size(max = 255) String titulo,
    @NotBlank @Size(max = 1000) String descripcion,
    @NotNull Boolean activa,
    @NotNull PrioridadAlerta prioridad) {}
