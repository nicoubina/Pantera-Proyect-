package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.Sector;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClaseRequest(
    @NotBlank @jakarta.validation.constraints.Size(max = 255) String nombre,
    @jakarta.validation.constraints.Size(max = 1000) String descripcion,
    @NotNull Long profesorId,
    @NotNull Sector sector,
    @Min(1) Integer cupoMaximo,
    Boolean activa) {}
