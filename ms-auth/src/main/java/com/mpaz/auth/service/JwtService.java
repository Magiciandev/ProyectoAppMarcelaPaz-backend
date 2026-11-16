package com.mpaz.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mpaz.auth.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Emite y lee tokens JWT firmados con HS256. El secreto se toma de la variable SECRET
 * (el mismo valor que usan el gateway y los demás servicios para validar).
 */
@Service
public class JwtService {

    public record TokenGenerado(String token, Instant expiraEn) {
    }

    private final SecretKey key;
    private final long vigenciaMs;

    public JwtService(@Value("${SECRET}") String secret,
                      @Value("${mpaz.jwt.horas:24}") long horas) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("SECRET debe tener al menos 32 caracteres");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.vigenciaMs = horas * 3_600_000L;
    }

    public TokenGenerado generar(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plusMillis(vigenciaMs);
        String token = Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("rol", usuario.getRol())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return new TokenGenerado(token, expira);
    }

    public Claims leer(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
