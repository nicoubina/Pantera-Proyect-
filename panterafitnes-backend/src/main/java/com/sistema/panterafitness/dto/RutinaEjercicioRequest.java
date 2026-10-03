package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record RutinaEjercicioRequest(
    @NotNull Long ejercicioId,
    @NotNull @Min(1) Integer series,
    @NotNull @Min(1) Integer repeticiones,
    @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal pesoSugerido,
    @NotNull @Min(0) Integer descanso) {}
