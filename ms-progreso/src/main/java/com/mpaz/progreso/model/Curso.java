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
@Table(name = "curso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Por ejemplo 2° A. */
    @Column(name = "nombre", nullable = false, unique = true, length = 20)
    private String nombre;

    /** Id del docente en ms-auth. */
    @Column(name = "docente_id", nullable = false)
    private Long docenteId;
}
