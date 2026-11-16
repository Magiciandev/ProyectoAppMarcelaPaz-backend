package com.mpaz.evaluaciones.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prueba_pregunta", uniqueConstraints = @UniqueConstraint(columnNames = {"prueba_id", "pregunta_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PruebaPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prueba_id", nullable = false)
    private Long pruebaId;

    @Column(name = "pregunta_id", nullable = false)
    private Long preguntaId;

    @Column(name = "orden_en_prueba", nullable = false)
    private Integer ordenEnPrueba;

    @Column(name = "puntaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntaje;
}
