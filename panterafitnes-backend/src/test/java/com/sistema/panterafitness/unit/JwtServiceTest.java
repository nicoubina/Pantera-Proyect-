package com.sistema.panterafitness.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.Rol;
import com.sistema.panterafitness.security.JwtService;
import io.jsonwebtoken.JwtException;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Pruebas unitarias del emisor/validador de tokens JWT (firma HS256 y expiracion). */
class JwtServiceTest {

  private JwtService jwt;
  private Usuario usuario;

  @BeforeEach
  void setUp() {
    jwt = new JwtService();
    ReflectionTestUtils.setField(jwt, "secret", Base64.getEncoder().encodeToString(new byte[32]).replace('A', 'k'));
    ReflectionTestUtils.setField(jwt, "expirationMs", 60_000L);
    usuario = Usuario.builder().id(7L).email("cliente@panterfitness.com").rol(Rol.CLIENTE).activo(true).build();
  }

  @Test
  @DisplayName("UT-06 El token emitido identifica al usuario y es valido para el mismo usuario")
  void tokenValido() {
    String token = jwt.generateToken(usuario);
    assertThat(jwt.extractUsername(token)).isEqualTo("cliente@panterfitness.com");
    assertThat(jwt.isTokenValid(token, usuario)).isTrue();
  }

  @Test
  @DisplayName("UT-07 Un token emitido para otro usuario no es valido")
  void tokenDeOtroUsuario() {
    String token = jwt.generateToken(usuario);
    Usuario otro = Usuario.builder().id(8L).email("otro@panterfitness.com").rol(Rol.CLIENTE).build();
    assertThat(jwt.isTokenValid(token, otro)).isFalse();
  }

  @Test
  @DisplayName("UT-08 Un token alterado (firma invalida) es rechazado")
  void tokenAlterado() {
    String token = jwt.generateToken(usuario);
    String alterado = token.substring(0, token.length() - 3) + (token.endsWith("abc") ? "xyz" : "abc");
    assertThatThrownBy(() -> jwt.extractUsername(alterado)).isInstanceOf(JwtException.class);
  }

  @Test
  @DisplayName("UT-09 Un token vencido es rechazado")
  void tokenVencido() {
    ReflectionTestUtils.setField(jwt, "expirationMs", -1_000L);
    String vencido = jwt.generateToken(usuario);
    assertThatThrownBy(() -> jwt.isTokenValid(vencido, usuario)).isInstanceOf(JwtException.class);
  }
}
