package com.mpaz.contenidos.dto;

import com.mpaz.contenidos.model.ObjetivoAprendizaje;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjetivoDTO {
    private Long id;
    private Long unidadId;
    private String codigo;
    private String descripcion;
    private String ejeCurricular;
    private String textoReferencia;
    private Integer orden;
    private String estado;

    public static ObjetivoDTO fromModel(ObjetivoAprendizaje o) {
        if (o == null) return null;
        return new ObjetivoDTO(o.getId(), o.getUnidadId(), o.getCodigo(), o.getDescripcion(),
                o.getEjeCurricular(), o.getTextoReferencia(), o.getOrden(), o.getEstado());
    }
}
