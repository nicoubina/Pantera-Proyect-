package com.sistema.panterafitness.controller;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.service.MembresiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/membresias")
@RequiredArgsConstructor
public class MembresiaController {
  private final MembresiaService service;

  @GetMapping("/mi-membresia")
  public MembresiaResponse propia() {
    return service.consultar(null);
  }

  @GetMapping("/{id}")
  public MembresiaResponse consultar(@PathVariable Long id) {
    return service.consultar(id);
  }
}
