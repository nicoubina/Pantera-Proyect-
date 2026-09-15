package com.sistema.panterafitness.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.sistema.panterafitness.enums.EstadoMembresia;
import com.sistema.panterafitness.enums.EstadoReserva;
import com.sistema.panterafitness.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class DataInitializerIntegrationTest {
    @Autowired DataInitializer initializer;
    @Autowired UsuarioRepository usuarios;
    @Autowired SectorGimnasioRepository sectores;
    @Autowired ClaseGimnasioRepository clases;
    @Autowired HorarioClaseRepository horarios;
    @Autowired ReservaRepository reservas;
    @Autowired ListaEsperaRepository espera;
    @Autowired NotificacionRepository notificaciones;
    @Autowired PasswordEncoder encoder;

    private long[] counts() {
        return new long[]{usuarios.count(), sectores.count(), clases.count(), horarios.count(),
                reservas.count(), espera.count(), notificaciones.count()};
    }

    @Test
    void repeatedStartupPreservesCountsPasswordsAndBusinessChanges() {
        long[] before = counts();
        var usuario = usuarios.findByEmail("cliente@panterfitness.com").orElseThrow();
        String hash = usuario.getPassword();
        usuario.setEstadoMembresia(EstadoMembresia.SUSPENDIDA);
        var reserva = reservas.findAll().stream()
                .filter(r -> r.getEstadoReserva() == EstadoReserva.CONFIRMADA).findFirst().orElseThrow();
        reserva.setEstadoReserva(EstadoReserva.CANCELADA);
        initializer.run();
        initializer.run();
        assertThat(counts()).containsExactly(before);
        assertThat(usuario.getPassword()).isEqualTo(hash);
        assertThat(encoder.matches("123456", hash)).isTrue();
        assertThat(usuario.getEstadoMembresia()).isEqualTo(EstadoMembresia.SUSPENDIDA);
        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void fillsMissingCatalogEvenWhenUsersAlreadyExist() {
        espera.deleteAll();
        reservas.deleteAll();
        horarios.deleteAll();
        clases.deleteAll();
        sectores.deleteAll();
        notificaciones.deleteAll();
        long usersBefore = usuarios.count();
        initializer.run();
        assertThat(usuarios.count()).isEqualTo(usersBefore);
        assertThat(sectores.count()).isEqualTo(2);
        assertThat(clases.count()).isEqualTo(2);
        assertThat(horarios.count()).isEqualTo(5);
        assertThat(reservas.count()).isEqualTo(21);
        assertThat(espera.count()).isEqualTo(1);
        long[] before = counts();
        initializer.run();
        assertThat(counts()).containsExactly(before);
    }
}
