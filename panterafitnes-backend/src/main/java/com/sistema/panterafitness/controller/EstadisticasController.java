package com.sistema.panterafitness.controller;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.service.EstadisticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticasController {
  private final EstadisticasService service;

  @GetMapping
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public EstadisticasResponse consultar() {
    return service.consultar();
  }
}
