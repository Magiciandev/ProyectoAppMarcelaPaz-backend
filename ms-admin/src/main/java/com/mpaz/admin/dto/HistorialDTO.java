package com.mpaz.admin.dto;

import java.time.Instant;

import com.mpaz.admin.model.HistorialAprobacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialDTO {
    private Long id;
    private String entidad;
    private Long entidadId;
    private Long responsableUtpId;
    private String accion;
    private String estadoAnterior;
    private String estadoNuevo;
    private Instant fecha;
    private String observacion;

    public static HistorialDTO fromModel(HistorialAprobacion h) {
        if (h == null) return null;
        return new HistorialDTO(h.getId(), h.getEntidad(), h.getEntidadId(), h.getResponsableUtpId(),
                h.getAccion(), h.getEstadoAnterior(), h.getEstadoNuevo(), h.getFecha(), h.getObservacion());
    }
}
