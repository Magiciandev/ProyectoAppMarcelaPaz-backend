package com.mpaz.progreso.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvanceDTO {
    private List<AvanceObjetivoDTO> objetivos;
    private Instant ultimoReinicio;
}
