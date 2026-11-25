package com.sistema.panterafitness.controller;

import com.sistema.panterafitness.dto.QrIngresoRequest;
import com.sistema.panterafitness.dto.QrIngresoResponse;
import com.sistema.panterafitness.service.QrIngresoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Control de acceso: recepcion (administrador) o el profesor escanean el QR del cliente. */
@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrController {

  private final QrIngresoService qrIngresoService;

  @PostMapping("/ingreso")
  @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR')")
  public QrIngresoResponse registrarIngreso(@Valid @RequestBody QrIngresoRequest request) {
    return qrIngresoService.registrarIngreso(request);
  }
}
