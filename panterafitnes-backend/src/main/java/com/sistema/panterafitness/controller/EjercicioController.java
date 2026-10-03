package com.sistema.panterafitness.controller;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.service.RutinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ejercicios")
@RequiredArgsConstructor
public class EjercicioController {
  private final RutinaService service;

  @GetMapping
  public java.util.List<EjercicioResponse> listar() {
    return service.ejercicios();
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR')")
  public EjercicioResponse crear(@Valid @RequestBody EjercicioRequest r) {
    return service.crearEjercicio(r);
  }
}
