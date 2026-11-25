package com.sistema.panterafitness.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint publico minimo para el chequeo de salud del hosting y del lanzador ejecutable. */
@RestController
@RequestMapping("/api/health")
public class HealthController {

  @GetMapping
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }
}
