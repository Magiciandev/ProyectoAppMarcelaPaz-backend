package com.mpaz.evaluaciones.model;

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
@Table(name = "actividad_desafio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDesafio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "objetivo_aprendizaje_id", nullable = false)
    private Long objetivoAprendizajeId;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    /** SELECCION, COMPLETACION, SECUENCIA o ASOCIACION. */
    @Column(name = "tipo", nullable = false, length = 15)
    private String tipo;

    /** De 1 (fácil) a 3 (difícil). */
    @Column(name = "dificultad", nullable = false)
    private Integer dificultad;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "estado", nullable = false, length = 15)
    private String estado;
}
