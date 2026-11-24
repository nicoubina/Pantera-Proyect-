package com.sistema.panterafitness.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.panterafitness.entity.HorarioClase;
import com.sistema.panterafitness.enums.EstadoReserva;
import com.sistema.panterafitness.repository.HorarioClaseRepository;
import com.sistema.panterafitness.repository.ReservaRepository;
import com.sistema.panterafitness.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de integracion del contrato HTTP: levantan Spring Boot completo (seguridad, controllers,
 * services, JPA) sobre H2 con los datos demo y verifican codigos de estado y cuerpos de respuesta.
 * Cada prueba corresponde a un caso de la matriz del Manual Tecnico (TC-xx).
 */
@SpringBootTest
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@Transactional
class ApiContractIntegrationTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired HorarioClaseRepository horarios;
  @Autowired ReservaRepository reservas;
  @Autowired UsuarioRepository usuarios;

  private String login(String email) throws Exception {
    String body =
        mvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + email + "\",\"password\":\"123456\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return "Bearer " + json.readTree(body).get("token").asText();
  }

  private String registrar(String email) throws Exception {
    String body =
        mvc.perform(
                post("/api/auth/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"nombre\":\"Prueba\",\"apellido\":\"API\",\"email\":\""
                            + email
                            + "\",\"password\":\"123456\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return "Bearer " + json.readTree(body).get("token").asText();
  }

  @Test
  @DisplayName("TC-01 Login con credenciales validas devuelve 200, token JWT y rol")
  void loginValido() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"cliente@panterfitness.com\",\"password\":\"123456\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.tipo").value("Bearer"))
        .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"))
        .andExpect(jsonPath("$.usuario.password").doesNotExist());
  }

  @Test
  @DisplayName("TC-02 Login con contrasena incorrecta devuelve 401 con mensaje generico")
  void loginInvalido() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"cliente@panterfitness.com\",\"password\":\"incorrecta\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Credenciales invalidas o token no valido."))
        .andExpect(content().string(not(containsString("Exception"))))
        .andExpect(content().string(not(containsString("trace"))));
  }

  @Test
  @DisplayName("TC-03 Registro con email de formato invalido devuelve 400 (validacion en backend)")
  void registroEmailInvalido() throws Exception {
    mvc.perform(
            post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ana\",\"email\":\"usuario@\",\"password\":\"123456\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("email")));
  }

  @Test
  @DisplayName("TC-04 Registro con email existente devuelve 400 y no duplica el usuario")
  void registroDuplicado() throws Exception {
    long antes = usuarios.count();
    mvc.perform(
            post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"nombre\":\"Ana\",\"email\":\"CLIENTE@panterfitness.com\",\"password\":\"123456\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("Ya existe")));
    assertThat(usuarios.count()).isEqualTo(antes);
  }

  @Test
  @DisplayName("TC-05 Una ruta privada sin token devuelve 401")
  void sinToken() throws Exception {
    mvc.perform(get("/api/reservas/mis-reservas")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("TC-06 Un CLIENTE no puede acceder a endpoints de administracion (403)")
  void clienteNoEsAdmin() throws Exception {
    String token = login("cliente@panterfitness.com");
    mvc.perform(get("/api/usuarios").header("Authorization", token)).andExpect(status().isForbidden());
    mvc.perform(get("/api/estadisticas").header("Authorization", token))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("TC-07 Un cliente no puede cancelar la reserva de otro usuario (403)")
  void cancelarReservaAjena() throws Exception {
    Long ajena =
        reservas.findAll().stream()
            .filter(r -> r.getUsuario().getEmail().equals("cupo01@panterfitness.com"))
            .findFirst()
            .orElseThrow()
            .getId();
    mvc.perform(
            delete("/api/reservas/" + ajena + "/cancelar")
                .header("Authorization", login("cliente@panterfitness.com")))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("TC-08 Un cliente no puede consultar la membresia de otro usuario (403)")
  void membresiaAjena() throws Exception {
    Long otro = usuarios.findByEmail("vencido@panterfitness.com").orElseThrow().getId();
    mvc.perform(
            get("/api/membresias/" + otro).header("Authorization", login("cliente@panterfitness.com")))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("TC-09 Un cliente con membresia vencida no puede reservar (400 con mensaje claro)")
  void membresiaVencidaNoReserva() throws Exception {
    HorarioClase horario = proximoHorarioCon(false);
    mvc.perform(
            post("/api/reservas")
                .header("Authorization", login("vencido@panterfitness.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horarioClaseId\":" + horario.getId() + "}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("membresia")));
  }

  @Test
  @DisplayName("TC-10 Reservar una clase sin cupo deja la reserva EN_ESPERA con posicion asignada")
  void claseLlenaVaAListaDeEspera() throws Exception {
    HorarioClase lleno = proximoHorarioCon(true);
    String token = registrar("espera-" + System.nanoTime() + "@example.test");
    String body =
        mvc.perform(
                post("/api/reservas")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"horarioClaseId\":" + lleno.getId() + "}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode reserva = json.readTree(body);
    assertThat(reserva.get("estadoReserva").asText()).isEqualTo("EN_ESPERA");
    assertThat(reserva.get("posicionListaEspera").asInt()).isGreaterThan(0);
  }

  @Test
  @DisplayName("TC-11 Un cuerpo JSON mal formado devuelve 400 sin exponer detalles internos")
  void jsonMalFormado() throws Exception {
    mvc.perform(
            post("/api/reservas")
                .header("Authorization", login("cliente@panterfitness.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{horarioClaseId: ???"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Revisa los campos, fechas y estados ingresados."))
        .andExpect(content().string(not(containsString("jackson"))));
  }

  @Test
  @DisplayName("TC-12 Un recurso inexistente devuelve 404 con mensaje controlado")
  void recursoInexistente() throws Exception {
    mvc.perform(
            delete("/api/reservas/999999/cancelar")
                .header("Authorization", login("cliente@panterfitness.com")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Reserva no encontrada."));
  }

  private HorarioClase proximoHorarioCon(boolean lleno) {
    List<EstadoReserva> ocupan = List.of(EstadoReserva.CONFIRMADA, EstadoReserva.ASISTIDA);
    return horarios.findByActivaTrueOrderByFechaAscHoraInicioAsc().stream()
        .filter(h -> !h.getFecha().isBefore(LocalDate.now().plusDays(1)))
        .filter(h -> !h.getFecha().isAfter(LocalDate.now().plusDays(6)))
        .filter(
            h ->
                (reservas.countByHorarioClaseIdAndEstadoReservaIn(h.getId(), ocupan)
                        >= h.getCupoMaximo())
                    == lleno)
        .findFirst()
        .orElseThrow();
  }
}
