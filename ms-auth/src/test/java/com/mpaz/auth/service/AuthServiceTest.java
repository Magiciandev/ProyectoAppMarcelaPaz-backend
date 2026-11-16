package com.mpaz.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mpaz.auth.dto.LoginRequest;
import com.mpaz.auth.dto.LoginResponse;
import com.mpaz.auth.exception.UnauthorizedException;
import com.mpaz.auth.model.Usuario;
import com.mpaz.auth.repository.UsuarioRepository;

class AuthServiceTest {

    private UsuarioRepository repository;
    private PasswordEncoder encoder;
    private AuthService service;

    @BeforeEach
    void preparar() {
        repository = mock(UsuarioRepository.class);
        encoder = new BCryptPasswordEncoder();
        JwtService jwt = new JwtService("un_secreto_de_prueba_con_mas_de_32_caracteres", 24);
        service = new AuthService(repository, encoder, jwt);
    }

    private Usuario usuario(boolean activo) {
        return new Usuario(3L, "ana", encoder.encode("1234"), "ESTUDIANTE", "Ana", activo);
    }

    @Test
    void loginCorrectoDevuelveTokenYDatos() {
        when(repository.findByUsuario("ana")).thenReturn(Optional.of(usuario(true)));

        LoginResponse r = service.login(new LoginRequest(" Ana ", "1234"));

        assertThat(r.getToken()).isNotBlank();
        assertThat(r.getTipo()).isEqualTo("Bearer");
        assertThat(r.getUsuarioId()).isEqualTo(3L);
        assertThat(r.getRol()).isEqualTo("ESTUDIANTE");
    }

    @Test
    void pinIncorrectoEsNoAutorizado() {
        when(repository.findByUsuario("ana")).thenReturn(Optional.of(usuario(true)));

        assertThatThrownBy(() -> service.login(new LoginRequest("ana", "0000")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Usuario o PIN incorrectos");
    }

    @Test
    void usuarioInexistenteUsaElMismoMensaje() {
        when(repository.findByUsuario("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("nadie", "1234")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Usuario o PIN incorrectos");
    }

    @Test
    void usuarioInactivoNoPuedeEntrar() {
        when(repository.findByUsuario("ana")).thenReturn(Optional.of(usuario(false)));

        assertThatThrownBy(() -> service.login(new LoginRequest("ana", "1234")))
                .isInstanceOf(UnauthorizedException.class);
    }
}
