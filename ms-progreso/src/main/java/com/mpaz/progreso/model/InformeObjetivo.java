package com.mpaz.progreso.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "informe_objetivo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformeObjetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "informe_id", nullable = false)
    private Long informeId;

    @Column(name = "objetivo_id", nullable = false)
    private Long objetivoId;

    @Column(name = "mejor_porcentaje", nullable = false)
    private Integer mejorPorcentaje;

    @Column(name = "nivel", nullable = false, length = 12)
    private String nivel;

    @Column(name = "intentos_aceptados", nullable = false)
    private Integer intentosAceptados;
}
