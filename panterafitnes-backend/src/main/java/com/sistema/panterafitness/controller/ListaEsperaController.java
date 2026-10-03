package com.sistema.panterafitness.controller;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.service.ListaEsperaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lista-espera")
@RequiredArgsConstructor
public class ListaEsperaController {
  private final ListaEsperaService service;

  @GetMapping
  public java.util.List<ListaEsperaResponse> listar(
      @RequestParam(required = false) Long horarioId) {
    return service.listar(horarioId);
  }
}
