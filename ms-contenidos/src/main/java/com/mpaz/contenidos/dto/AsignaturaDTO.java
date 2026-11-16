package com.mpaz.contenidos.dto;

import com.mpaz.contenidos.model.Asignatura;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignaturaDTO {
    private Long id;
    private String nombre;
    private String nivel;

    public static AsignaturaDTO fromModel(Asignatura a) {
        if (a == null) return null;
        return new AsignaturaDTO(a.getId(), a.getNombre(), a.getNivel());
    }
}
