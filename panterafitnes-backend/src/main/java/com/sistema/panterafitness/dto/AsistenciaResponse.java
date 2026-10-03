package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record AsistenciaResponse(
    Long id,
    UsuarioResumenResponse usuario,
    Long reservaId,
    HorarioClaseResponse horarioClase,
    LocalDate fecha,
    LocalTime horaProgramada,
    LocalDateTime horaIngreso,
    EstadoAsistencia estadoAsistencia,
    MetodoRegistro metodoRegistro) {}
