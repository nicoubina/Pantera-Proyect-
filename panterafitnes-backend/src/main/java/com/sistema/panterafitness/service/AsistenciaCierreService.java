package com.sistema.panterafitness.service;

import com.sistema.panterafitness.enums.*;
import com.sistema.panterafitness.repository.ReservaRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AsistenciaCierreService {
  private final ReservaRepository reservas;
  private final AsistenciaService asistencias;

  @Transactional
  public void cerrar(Long id) {
    var r = reservas.findLockedById(id).orElseThrow();
    if (r.getEstadoReserva() == EstadoReserva.CONFIRMADA
        && r.getHorarioClase()
            .getFecha()
            .atTime(r.getHorarioClase().getHoraFin())
            .isBefore(LocalDateTime.now()))
      asistencias.registrarReserva(r, EstadoAsistencia.AUSENTE, null, MetodoRegistro.AUTOMATICO);
  }
}
