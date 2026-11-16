package com.mpaz.progreso.model;

import java.math.BigDecimal;
import java.time.Instant;

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
@Table(name = "respuesta_estudiante")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaEstudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "intento_id", nullable = false)
    private Long intentoId;

    @Column(name = "pregunta_id", nullable = false)
    private Long preguntaId;

    /** Texto JSON: la opción elegida, el orden elegido o los pares unidos. */
    @Column(name = "respuesta", nullable = false, columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta;

    @Column(name = "puntaje_obtenido", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeObtenido;

    @Column(name = "marca_tiempo", nullable = false)
    private Instant marcaTiempo;
}
