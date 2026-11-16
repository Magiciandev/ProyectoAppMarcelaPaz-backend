package com.mpaz.auth.dto;

import com.mpaz.auth.model.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos del usuario que se pueden mostrar: no incluye el PIN ni su hash. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long usuarioId;
    private String usuario;
    private String nombre;
    private String rol;

    public static UsuarioDTO fromModel(Usuario u) {
        if (u == null) return null;
        return new UsuarioDTO(u.getId(), u.getUsuario(), u.getNombre(), u.getRol());
    }
}
