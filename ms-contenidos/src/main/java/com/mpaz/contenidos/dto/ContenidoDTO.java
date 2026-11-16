package com.mpaz.contenidos.dto;

import com.mpaz.contenidos.model.ContenidoDidactico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoDTO {
    private Long id;
    private Long objetivoId;
    private String titulo;
    private String explicacion;
    private String instrucciones;
    private String ejemplosGuiados;
    private String imagenUrl;
    private String archivoAudio;
    private Integer orden;
    private String responsableOrigen;
    private String licencia;

    public static ContenidoDTO fromModel(ContenidoDidactico c) {
        if (c == null) return null;
        return new ContenidoDTO(c.getId(), c.getObjetivoId(), c.getTitulo(), c.getExplicacion(),
                c.getInstrucciones(), c.getEjemplosGuiados(), c.getImagenUrl(), c.getArchivoAudio(),
                c.getOrden(), c.getResponsableOrigen(), c.getLicencia());
    }
}
