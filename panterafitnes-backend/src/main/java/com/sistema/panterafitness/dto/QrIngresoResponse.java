package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.EstadoAsistencia;
import java.time.LocalDateTime;

public record QrIngresoResponse(
    String mensaje,
    Long usuarioId,
    String cliente,
    Long reservaId,
    Long horarioClaseId,
    String clase,
    EstadoAsistencia estadoAsistencia,
    LocalDateTime horaIngreso) {}
