package com.mpaz.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tipo;
    /** Instante de vencimiento en ISO-8601 UTC. */
    private String expiraEn;
    private Long usuarioId;
    private String nombre;
    private String rol;
}
