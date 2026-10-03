package com.sistema.panterafitness.service;

import com.sistema.panterafitness.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AsistenciaScheduler {
  private final ReservaRepository reservas;
  private final AsistenciaCierreService cierre;

  @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 60000, initialDelay = 60000)
  public void cerrarClases() {
    for (Long id :
        reservas.findConfirmadasFinalizadas(java.time.LocalDate.now(), java.time.LocalTime.now())) {
      try {
        cierre.cerrar(id);
      } catch (RuntimeException e) {
        log.warn("No se pudo cerrar asistencia de reserva {}", id, e);
      }
    }
  }
}
