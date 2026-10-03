package com.sistema.panterafitness.controller;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.service.AlertaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {
  private final AlertaService service;

  @GetMapping
  public java.util.List<AlertaResponse> listar() {
    return service.listar();
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public AlertaResponse crear(@Valid @RequestBody AlertaRequest r) {
    return service.guardar(null, r);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public AlertaResponse editar(@PathVariable Long id, @Valid @RequestBody AlertaRequest r) {
    return service.guardar(id, r);
  }
}
