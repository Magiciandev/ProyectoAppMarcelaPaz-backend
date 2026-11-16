package com.mpaz.evaluaciones.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mpaz.evaluaciones.model.Opcion;
import com.mpaz.evaluaciones.model.TipoPregunta;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpcionDTO {
    private Long id;

    @NotBlank(message = "El texto de la opción es obligatorio")
    private String texto;

    /** Solo SELECCION y COMPLETACION. */
    private Boolean esCorrecta;

    /** Solo SECUENCIA. */
    private Integer posicionCorrecta;

    /** Solo ASOCIACION. */
    private String parClave;

    private Integer orden;

    public static OpcionDTO fromModel(Opcion o, TipoPregunta tipo) {
        OpcionDTO dto = new OpcionDTO();
        dto.setId(o.getId());
        dto.setTexto(o.getTexto());
        dto.setOrden(o.getOrden());
        switch (tipo) {
            case SELECCION, COMPLETACION -> dto.setEsCorrecta(o.isEsCorrecta());
            case SECUENCIA -> dto.setPosicionCorrecta(o.getPosicionCorrecta());
            case ASOCIACION -> dto.setParClave(o.getParClave());
        }
        return dto;
    }
}
