package com.mpaz.progreso.model;

import java.math.BigDecimal;

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
@Table(name = "informe_respuesta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformeRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "informe_id", nullable = false)
    private Long informeId;

    @Column(name = "objetivo_id", nullable = false)
    private Long objetivoId;

    @Column(name = "pregunta_id", nullable = false)
    private Long preguntaId;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta;

    @Column(name = "puntaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntaje;
}
