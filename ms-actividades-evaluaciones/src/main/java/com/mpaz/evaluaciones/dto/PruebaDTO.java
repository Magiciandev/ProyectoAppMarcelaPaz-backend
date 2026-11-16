package com.mpaz.evaluaciones.dto;

import java.util.List;

import com.mpaz.evaluaciones.model.PruebaFinal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PruebaDTO {
    private Long id;
    private Long objetivoId;
    private String titulo;
    private String criteriosPuntuacion;
    private Integer limiteIntentosPorDefecto;
    private String estado;
    private List<PreguntaEnPruebaDTO> preguntas;

    public static PruebaDTO fromModel(PruebaFinal p, List<PreguntaEnPruebaDTO> preguntas) {
        return new PruebaDTO(p.getId(), p.getObjetivoAprendizajeId(), p.getTitulo(), p.getCriteriosPuntuacion(),
                p.getLimiteIntentos(), p.getEstado(), preguntas);
    }
}
