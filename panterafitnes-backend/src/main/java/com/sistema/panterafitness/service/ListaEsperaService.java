package com.sistema.panterafitness.service;

import com.sistema.panterafitness.dto.*;
import com.sistema.panterafitness.enums.EstadoReserva;
import com.sistema.panterafitness.mapper.EntityMapper;
import com.sistema.panterafitness.repository.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListaEsperaService {
  private final ListaEsperaRepository repository;
  private final ReservaRepository reservas;
  private final AccesoService acceso;

  @Transactional(readOnly = true)
  public List<ListaEsperaResponse> listar(Long horarioId) {
    return repository.findAll().stream()
        .filter(l -> acceso.visible(l.getHorarioClase(), l.getUsuario().getId()))
        .filter(l -> horarioId == null || l.getHorarioClase().getId().equals(horarioId))
        .map(
            l -> {
              var estado =
                  reservas.findByUsuarioIdOrderByFechaCreacionDesc(l.getUsuario().getId()).stream()
                      .filter(
                          r ->
                              r.getHorarioClase().getId().equals(l.getHorarioClase().getId())
                                  && !r.getFechaCreacion()
                                      .isBefore(l.getFechaIngreso().minusSeconds(2)))
                      .map(r -> r.getEstadoReserva())
                      .findFirst()
                      .orElse(EstadoReserva.CANCELADA);
              return new ListaEsperaResponse(
                  l.getId(),
                  EntityMapper.toUsuarioResumenResponse(l.getUsuario()),
                  EntityMapper.toHorarioClaseResponse(l.getHorarioClase()),
                  l.getFechaIngreso(),
                  Boolean.TRUE.equals(l.getActiva()) ? l.getPosicion() : null,
                  l.getActiva(),
                  estado);
            })
        .toList();
  }
}
