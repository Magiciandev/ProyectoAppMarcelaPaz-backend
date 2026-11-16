package com.mpaz.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.mpaz.auth.model.Usuario;

import io.jsonwebtoken.Claims;

class JwtServiceTest {

    private static final String SECRETO = "un_secreto_de_prueba_con_mas_de_32_caracteres";

    private Usuario usuario() {
        return new Usuario(7L, "ana", "hash", "ESTUDIANTE", "Ana", true);
    }

    @Test
    void generaTokenConIdYRolYSePuedeLeer() {
        JwtService service = new JwtService(SECRETO, 24);

        JwtService.TokenGenerado generado = service.generar(usuario());
        Claims claims = service.leer(generado.token());

        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.get("rol", String.class)).isEqualTo("ESTUDIANTE");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void rechazaSecretoCorto() {
        assertThatThrownBy(() -> new JwtService("corto", 24))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rechazaTokenFirmadoConOtroSecreto() {
        JwtService emisor = new JwtService(SECRETO, 24);
        JwtService otro = new JwtService("otro_secreto_distinto_de_mas_de_32_caracteres", 24);

        String token = emisor.generar(usuario()).token();

        assertThatThrownBy(() -> otro.leer(token)).isInstanceOf(RuntimeException.class);
    }
}
