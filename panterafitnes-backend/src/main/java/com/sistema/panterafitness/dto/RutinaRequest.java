package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record RutinaRequest(
    @NotBlank @Size(max = 255) String nombre,
    @Size(max = 1000) String descripcion,
    @NotNull Long clienteId,
    @NotNull EstadoRutina estado,
    @NotNull @Size(min = 1, max = 100)
        java.util.List<@jakarta.validation.Valid RutinaEjercicioRequest> ejercicios) {}
