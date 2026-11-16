package com.mpaz.progreso.seguridad;

import org.springframework.security.oauth2.jwt.Jwt;

import com.mpaz.progreso.model.Rol;

/** Lee del token los datos de quien hace la petición. */
public final class Sesion {

    private Sesion() {
    }

    public static Rol rol(Jwt jwt) {
        return Rol.de(jwt.getClaimAsString("rol"));
    }

    public static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
