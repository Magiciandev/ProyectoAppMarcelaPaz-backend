package com.mpaz.progreso.model;

import java.time.Instant;
import java.time.LocalDate;

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
@Table(name = "asignacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "objetivo_aprendizaje_id", nullable = false)
    private Long objetivoAprendizajeId;

    /** Una de las dos: curso o estudiante. */
    @Column(name = "curso_id")
    private Long cursoId;

    @Column(name = "estudiante_id")
    private Long estudianteId;

    @Column(name = "docente_id", nullable = false)
    private Long docenteId;

    @Column(name = "disponible", nullable = false)
    private boolean disponible;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDate fechaHasta;

    @Column(name = "limite_intentos", nullable = false)
    private Integer limiteIntentos;

    /** Prueba final. */
    @Column(name = "fecha_habilitacion")
    private LocalDate fechaHabilitacion;

    @Column(name = "fecha_rendicion")
    private LocalDate fechaRendicion;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
}
