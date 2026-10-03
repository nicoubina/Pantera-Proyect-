package com.sistema.panterafitness.controller;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.service.AsistenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asistencias")
@RequiredArgsConstructor
public class AsistenciaController {
  private final AsistenciaService service;

  @GetMapping
  public java.util.List<AsistenciaResponse> listar(
      @RequestParam(required = false) Long usuarioId,
      @RequestParam(required = false) Long horarioId,
      @RequestParam(required = false) com.sistema.panterafitness.enums.EstadoAsistencia estado) {
    return service.listar(usuarioId, horarioId, estado);
  }

  @GetMapping("/{id}")
  public AsistenciaResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR')")
  public AsistenciaResponse registrar(@Valid @RequestBody RegistrarAsistenciaRequest r) {
    return service.registrar(r);
  }
}
