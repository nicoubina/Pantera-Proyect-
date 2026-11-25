package com.sistema.panterafitness.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.EstadoMembresia;
import com.sistema.panterafitness.exception.BusinessException;
import com.sistema.panterafitness.service.MembresiaService;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias (sin Spring ni base de datos) de la regla de vigencia de membresias. */
class MembresiaServiceTest {

  private final MembresiaService service = new MembresiaService(null, null);

  private Usuario cliente(EstadoMembresia estado, LocalDate inicio, LocalDate vencimiento) {
    return Usuario.builder()
        .estadoMembresia(estado)
        .fechaInicioMembresia(inicio)
        .fechaVencimientoMembresia(vencimiento)
        .activo(true)
        .build();
  }

  @Test
  @DisplayName("UT-01 Membresia ACTIVA dentro de sus fechas se considera vigente")
  void activaVigente() {
    Usuario u = cliente(EstadoMembresia.ACTIVA, LocalDate.now().minusDays(5), LocalDate.now().plusDays(25));
    assertThat(MembresiaService.estadoActual(u)).isEqualTo(EstadoMembresia.ACTIVA);
    assertThatCode(() -> service.validarReserva(u)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("UT-02 Membresia marcada ACTIVA pero con fecha vencida se informa VENCIDA y bloquea reservas")
  void activaConFechaVencida() {
    Usuario u = cliente(EstadoMembresia.ACTIVA, LocalDate.now().minusMonths(2), LocalDate.now().minusDays(1));
    assertThat(MembresiaService.estadoActual(u)).isEqualTo(EstadoMembresia.VENCIDA);
    assertThatThrownBy(() -> service.validarReserva(u))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("ACTIVA");
  }

  @Test
  @DisplayName("UT-03 Membresia que empieza en el futuro se informa PENDIENTE")
  void inicioFuturo() {
    Usuario u = cliente(EstadoMembresia.ACTIVA, LocalDate.now().plusDays(3), LocalDate.now().plusMonths(1));
    assertThat(MembresiaService.estadoActual(u)).isEqualTo(EstadoMembresia.PENDIENTE);
  }

  @Test
  @DisplayName("UT-04 Un usuario desactivado no puede reservar aunque su membresia este vigente")
  void usuarioInactivo() {
    Usuario u = cliente(EstadoMembresia.ACTIVA, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
    u.setActivo(false);
    assertThatThrownBy(() -> service.validarReserva(u)).isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("UT-05 Un estado administrativo (SUSPENDIDA) se respeta sin recalcular por fechas")
  void suspendida() {
    Usuario u = cliente(EstadoMembresia.SUSPENDIDA, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
    assertThat(MembresiaService.estadoActual(u)).isEqualTo(EstadoMembresia.SUSPENDIDA);
  }
}
