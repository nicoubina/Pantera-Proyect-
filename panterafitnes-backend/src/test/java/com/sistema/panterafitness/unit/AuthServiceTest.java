package com.sistema.panterafitness.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sistema.panterafitness.dto.RegistroRequest;
import com.sistema.panterafitness.entity.Usuario;
import com.sistema.panterafitness.enums.EstadoMembresia;
import com.sistema.panterafitness.enums.Rol;
import com.sistema.panterafitness.exception.BusinessException;
import com.sistema.panterafitness.repository.UsuarioRepository;
import com.sistema.panterafitness.security.JwtService;
import com.sistema.panterafitness.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Pruebas unitarias del registro con dependencias simuladas (Mockito). */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UsuarioRepository usuarioRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock AuthenticationManager authenticationManager;
  @Mock JwtService jwtService;
  @InjectMocks AuthService authService;

  @Test
  @DisplayName("UT-10 El registro rechaza un email ya existente sin guardar nada")
  void emailDuplicado() {
    when(usuarioRepository.existsByEmail("ana@mail.com")).thenReturn(true);
    assertThatThrownBy(
            () -> authService.registrar(new RegistroRequest("Ana", "Perez", " Ana@Mail.com ", "123456")))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Ya existe");
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  @DisplayName("UT-11 El registro normaliza el email, guarda la contrasena hasheada y asigna rol CLIENTE")
  void registroCorrecto() {
    PasswordEncoder bcrypt = new BCryptPasswordEncoder();
    when(usuarioRepository.existsByEmail("ana@mail.com")).thenReturn(false);
    when(passwordEncoder.encode("123456")).thenAnswer(inv -> bcrypt.encode(inv.getArgument(0)));
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    when(jwtService.generateToken(any())).thenReturn("token");

    var respuesta = authService.registrar(new RegistroRequest(" Ana ", null, " Ana@Mail.com ", "123456"));

    ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
    verify(usuarioRepository).save(guardado.capture());
    Usuario u = guardado.getValue();
    assertThat(u.getEmail()).isEqualTo("ana@mail.com");
    assertThat(u.getRol()).isEqualTo(Rol.CLIENTE);
    assertThat(u.getEstadoMembresia()).isEqualTo(EstadoMembresia.ACTIVA);
    assertThat(u.getPassword()).isNotEqualTo("123456").startsWith("$2");
    assertThat(bcrypt.matches("123456", u.getPassword())).isTrue();
    assertThat(u.getQrSimulado()).startsWith("PF-");
    assertThat(respuesta.token()).isEqualTo("token");
  }
}
