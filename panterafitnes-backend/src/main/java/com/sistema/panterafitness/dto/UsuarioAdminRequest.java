package com.sistema.panterafitness.dto;

import com.sistema.panterafitness.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record UsuarioAdminRequest(
    @NotBlank @Size(max = 255) String nombre,
    @Size(max = 255) String apellido,
    @NotBlank @Email @Size(max = 255) String email,
    @Size(min = 6, max = 72) String password,
    @NotNull Rol rol,
    @NotNull Boolean activo) {}
