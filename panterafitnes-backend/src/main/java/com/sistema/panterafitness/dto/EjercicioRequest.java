package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record EjercicioRequest(
    @NotBlank @Size(max = 255) String nombre, @Size(max = 1000) String descripcion) {}
