package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record AlertaResponse(
    Long id,
    String titulo,
    String descripcion,
    LocalDateTime fechaCreacion,
    Boolean activa,
    PrioridadAlerta prioridad) {}
