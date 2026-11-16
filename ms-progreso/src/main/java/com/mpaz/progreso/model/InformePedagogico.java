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
@Table(name = "informe_pedagogico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformePedagogico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estudiante_id", nullable = false)
    private Long estudianteId;

    @Column(name = "curso_id", nullable = false)
    private Long cursoId;

    /** Nulo = informe global. */
    @Column(name = "objetivo_aprendizaje_id")
    private Long objetivoAprendizajeId;

    @Column(name = "puntaje_global", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeGlobal;

    /** DOMINA, EN_VIAS o REFUERZO. */
    @Column(name = "nivel", nullable = false, length = 12)
    private String nivel;

    @Column(name = "fecha_emision", nullable = false)
    private Instant fechaEmision;

    @Column(name = "validado", nullable = false)
    private boolean validado;

    @Column(name = "validado_por")
    private Long validadoPor;

    @Column(name = "fecha_validacion")
    private Instant fechaValidacion;

    @Column(name = "observacion_docente", columnDefinition = "TEXT")
    private String observacionDocente;
}
