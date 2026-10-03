package com.sistema.panterafitness.controller;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.service.RutinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rutinas")
@RequiredArgsConstructor
public class RutinaController {
  private final RutinaService service;

  @GetMapping
  public java.util.List<RutinaResponse> listar() {
    return service.listar();
  }

  @GetMapping("/clientes")
  @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR')")
  public java.util.List<UsuarioResumenResponse> clientes() {
    return service.clientes();
  }

  @GetMapping("/{id}")
  public RutinaResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('PROFESOR')")
  public RutinaResponse crear(@Valid @RequestBody RutinaRequest r) {
    return service.guardar(null, r);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('PROFESOR')")
  public RutinaResponse editar(@PathVariable Long id, @Valid @RequestBody RutinaRequest r) {
    return service.guardar(id, r);
  }
}
