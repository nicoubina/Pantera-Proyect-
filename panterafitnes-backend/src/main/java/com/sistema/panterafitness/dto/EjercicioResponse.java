package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record EjercicioResponse(Long id, String nombre, String descripcion) {}
