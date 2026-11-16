package com.mpaz.evaluaciones.dto;

import java.util.List;

import com.mpaz.evaluaciones.model.Opcion;
import com.mpaz.evaluaciones.model.Pregunta;
import com.mpaz.evaluaciones.model.TipoPregunta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaDTO {
    private Long id;
    private Long objetivoId;
    private String tipo;
    private String enunciado;
    private String explicacion;
    private List<OpcionDTO> opciones;

    public static PreguntaDTO fromModel(Pregunta p, List<Opcion> opciones) {
        TipoPregunta tipo = TipoPregunta.de(p.getTipo());
        List<OpcionDTO> lista = opciones.stream().map(o -> OpcionDTO.fromModel(o, tipo)).toList();
        return new PreguntaDTO(p.getId(), p.getObjetivoAprendizajeId(), p.getTipo(), p.getEnunciado(),
                p.getExplicacion(), lista);
    }
}
