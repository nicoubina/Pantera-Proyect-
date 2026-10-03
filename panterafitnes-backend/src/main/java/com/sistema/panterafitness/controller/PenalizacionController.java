package com.sistema.panterafitness.controller;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.service.PenalizacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/penalizaciones")
@RequiredArgsConstructor
public class PenalizacionController {
  private final PenalizacionService service;

  @GetMapping
  @PreAuthorize("hasAnyRole('CLIENTE','ADMINISTRADOR')")
  public java.util.List<PenalizacionResponse> listar() {
    return service.listar();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CLIENTE','ADMINISTRADOR')")
  public PenalizacionResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public PenalizacionResponse crear(@Valid @RequestBody PenalizacionRequest r) {
    return service.crear(r);
  }

  @PatchMapping("/{id}/cancelar")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public PenalizacionResponse cancelar(@PathVariable Long id) {
    return service.cancelar(id);
  }
}
