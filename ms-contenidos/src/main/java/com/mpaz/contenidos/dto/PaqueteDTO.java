package com.mpaz.contenidos.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Un objetivo con todos sus contenidos, pensado para guardarse en la caché de la app. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaqueteDTO {
    private ObjetivoDTO objetivo;
    private List<ContenidoDTO> contenidos;
}
