package com.sistema.panterafitness.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sistema.panterafitness.dto.CrearReservaRequest;
import com.sistema.panterafitness.entity.ClaseGimnasio;
import com.sistema.panterafitness.entity.HorarioClase;
import com.sistema.panterafitness.entity.ListaEspera;
import com.sistema.panterafitness.entity.Reserva;
import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.DiaSemana;
import com.sistema.panterafitness.enums.EstadoMembresia;
import com.sistema.panterafitness.enums.EstadoReserva;
import com.sistema.panterafitness.enums.Rol;
import com.sistema.panterafitness.enums.Sector;
import com.sistema.panterafitness.exception.BusinessException;
import com.sistema.panterafitness.exception.ForbiddenException;
import com.sistema.panterafitness.repository.HorarioClaseRepository;
import com.sistema.panterafitness.repository.ListaEsperaRepository;
import com.sistema.panterafitness.repository.ReservaRepository;
import com.sistema.panterafitness.service.AsistenciaService;
import com.sistema.panterafitness.service.MembresiaService;
import com.sistema.panterafitness.service.NotificacionService;
import com.sistema.panterafitness.service.PenalizacionService;
import com.sistema.panterafitness.service.ReservaService;
import com.sistema.panterafitness.service.UsuarioService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias de las reglas de reserva. Los repositorios y servicios colaboradores se
 * simulan con Mockito: se prueba solo la logica de negocio, sin base de datos.
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

  @Mock ReservaRepository reservaRepository;
  @Mock HorarioClaseRepository horarioClaseRepository;
  @Mock ListaEsperaRepository listaEsperaRepository;
  @Mock UsuarioService usuarioService;
  @Mock NotificacionService notificacionService;
  @Mock AsistenciaService asistenciaService;
  @Mock MembresiaService membresiaService;
  @Mock PenalizacionService penalizacionService;
  @InjectMocks ReservaService reservaService;

  private Usuario cliente;
  private HorarioClase horario;

  @BeforeEach
  void setUp() {
    cliente =
        Usuario.builder()
            .id(1L)
            .nombre("Ana")
            .apellido("Perez")
            .email("ana@mail.com")
            .rol(Rol.CLIENTE)
            .estadoMembresia(EstadoMembresia.ACTIVA)
            .fechaInicioMembresia(LocalDate.now().minusDays(5))
            .fechaVencimientoMembresia(LocalDate.now().plusDays(25))
            .activo(true)
            .build();
    Usuario profesor = Usuario.builder().id(2L).nombre("Profe").apellido("X").rol(Rol.PROFESOR).build();
    ClaseGimnasio clase =
        ClaseGimnasio.builder()
            .id(10L)
            .nombre("Funcional")
            .profesor(profesor)
            .sector(Sector.SALA_CLASES)
            .cupoMaximo(2)
            .activa(true)
            .build();
    LocalDateTime inicio = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
    horario =
        HorarioClase.builder()
            .id(100L)
            .claseGimnasio(clase)
            .diaSemana(DiaSemana.from(inicio.getDayOfWeek()))
            .fecha(inicio.toLocalDate())
            .horaInicio(inicio.toLocalTime())
            .horaFin(inicio.toLocalTime().plusMinutes(1))
            .cupoMaximo(2)
            .activa(true)
            .build();
    lenient().when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(cliente);
    lenient().when(horarioClaseRepository.findLockedById(100L)).thenReturn(Optional.of(horario));
    lenient().when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  @DisplayName("UT-12 Solo el rol CLIENTE puede reservar (un PROFESOR recibe 403)")
  void profesorNoReserva() {
    cliente.setRol(Rol.PROFESOR);
    assertThatThrownBy(() -> reservaService.crearReserva(new CrearReservaRequest(100L)))
        .isInstanceOf(ForbiddenException.class);
    verify(reservaRepository, never()).save(any());
  }

  @Test
  @DisplayName("UT-13 No se puede reservar con mas de una semana de anticipacion")
  void maximoUnaSemana() {
    horario.setFecha(LocalDate.now().plusDays(9));
    assertThatThrownBy(() -> reservaService.crearReserva(new CrearReservaRequest(100L)))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("1 semana");
  }

  @Test
  @DisplayName("UT-14 No se puede reservar dos veces el mismo horario")
  void reservaDuplicada() {
    when(reservaRepository.existsByUsuarioIdAndHorarioClaseIdAndEstadoReservaIn(eq(1L), eq(100L), anyList()))
        .thenReturn(true);
    assertThatThrownBy(() -> reservaService.crearReserva(new CrearReservaRequest(100L)))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("ya tiene una reserva");
  }

  @Test
  @DisplayName("UT-15 Con cupo disponible la reserva queda CONFIRMADA y genera asistencia pendiente")
  void confirmadaConCupo() {
    when(reservaRepository.countByHorarioClaseIdAndEstadoReservaIn(eq(100L), anyList())).thenReturn(1L);
    var respuesta = reservaService.crearReserva(new CrearReservaRequest(100L));
    assertThat(respuesta.estadoReserva()).isEqualTo(EstadoReserva.CONFIRMADA);
    verify(asistenciaService).pendiente(any(Reserva.class));
    verify(listaEsperaRepository, never()).save(any());
  }

  @Test
  @DisplayName("UT-16 Sin cupo la reserva queda EN_ESPERA con la siguiente posicion de la lista")
  void listaDeEsperaSinCupo() {
    when(reservaRepository.countByHorarioClaseIdAndEstadoReservaIn(eq(100L), anyList())).thenReturn(2L);
    when(listaEsperaRepository.countByHorarioClaseIdAndActivaTrue(100L)).thenReturn(3L);
    lenient()
        .when(listaEsperaRepository.findByUsuarioIdAndHorarioClaseIdAndActivaTrue(anyLong(), anyLong()))
        .thenReturn(Optional.empty());

    var respuesta = reservaService.crearReserva(new CrearReservaRequest(100L));

    assertThat(respuesta.estadoReserva()).isEqualTo(EstadoReserva.EN_ESPERA);
    ArgumentCaptor<ListaEspera> espera = ArgumentCaptor.forClass(ListaEspera.class);
    verify(listaEsperaRepository).save(espera.capture());
    assertThat(espera.getValue().getPosicion()).isEqualTo(4);
    verify(asistenciaService, never()).pendiente(any());
  }

  @Test
  @DisplayName("UT-17 Un cliente con penalizacion activa no puede reservar con anticipacion")
  void penalizadoNoReservaAnticipado() {
    when(penalizacionService.tieneBloqueo(cliente)).thenReturn(true);
    assertThatThrownBy(() -> reservaService.crearReserva(new CrearReservaRequest(100L)))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("penalizacion");
  }

  @Test
  @DisplayName("UT-18 No se permite reservar dos clases superpuestas")
  void superposicion() {
    HorarioClase otro =
        HorarioClase.builder()
            .id(200L)
            .claseGimnasio(horario.getClaseGimnasio())
            .fecha(horario.getFecha())
            .horaInicio(horario.getHoraInicio())
            .horaFin(horario.getHoraFin())
            .cupoMaximo(10)
            .activa(true)
            .build();
    when(reservaRepository.findByUsuarioIdAndEstadoReservaIn(eq(1L), anyList()))
        .thenReturn(List.of(Reserva.builder().usuario(cliente).horarioClase(otro).build()));
    assertThatThrownBy(() -> reservaService.crearReserva(new CrearReservaRequest(100L)))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("superpuestas");
  }
}
