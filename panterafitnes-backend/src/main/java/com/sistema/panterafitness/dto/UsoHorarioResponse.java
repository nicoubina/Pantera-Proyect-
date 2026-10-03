package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record UsoHorarioResponse(
    Long horarioId, String clase, LocalDate fecha, LocalTime hora, long reservas) {}
