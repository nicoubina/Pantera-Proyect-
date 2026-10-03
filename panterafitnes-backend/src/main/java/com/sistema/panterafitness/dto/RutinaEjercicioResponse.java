package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record RutinaEjercicioResponse(
    Long id,
    EjercicioResponse ejercicio,
    Integer series,
    Integer repeticiones,
    BigDecimal pesoSugerido,
    Integer descanso,
    Integer orden) {}
