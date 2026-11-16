package com.mpaz.auth.model;

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
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario", nullable = false, unique = true, length = 40)
    private String usuario;

    /** PIN guardado con BCrypt; nunca se devuelve en una respuesta. */
    @Column(name = "pin_hash", nullable = false, length = 100)
    private String pinHash;

    /** UTP, DOCENTE o ESTUDIANTE. */
    @Column(name = "rol", nullable = false, length = 12)
    private String rol;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}
