package com.mpaz.progreso.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estudiante")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estudiante {

    /** Id del usuario en ms-auth (no se genera aquí). */
    @Id
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "curso_id", nullable = false)
    private Long cursoId;

    @Column(name = "ultima_conexion")
    private Instant ultimaConexion;

    @Column(name = "ultima_sincronizacion")
    private Instant ultimaSincronizacion;
}
