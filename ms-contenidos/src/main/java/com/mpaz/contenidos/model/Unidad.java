package com.mpaz.contenidos.model;

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
@Table(name = "unidad")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asignatura_id", nullable = false)
    private Long asignaturaId;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    /** Derivado de los estados de sus objetivos (ver EstadoUnidadCalculator). */
    @Column(name = "estado", nullable = false, length = 15)
    private String estado;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    /** Identificador del usuario UTP en ms-auth. */
    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
}
