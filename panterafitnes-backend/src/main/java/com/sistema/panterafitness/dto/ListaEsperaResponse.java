package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record ListaEsperaResponse(
    Long id,
    UsuarioResumenResponse usuario,
    HorarioClaseResponse horarioClase,
    LocalDateTime fechaIngreso,
    Integer posicion,
    Boolean activa,
    EstadoReserva estado) {}
