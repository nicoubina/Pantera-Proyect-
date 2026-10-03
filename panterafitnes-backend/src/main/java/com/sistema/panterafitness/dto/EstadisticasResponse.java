package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record EstadisticasResponse(
    long usuarios,
    long membresiasActivas,
    long membresiasVencidas,
    long clases,
    long reservas,
    long asistencias,
    long ausencias,
    long listaEspera,
    OcupacionGeneralResponse ocupacion,
    java.util.List<PopularidadResponse> clasesPopulares,
    java.util.List<UsoHorarioResponse> horariosUtilizados,
    java.util.List<ProfesorClasesResponse> profesores) {}
