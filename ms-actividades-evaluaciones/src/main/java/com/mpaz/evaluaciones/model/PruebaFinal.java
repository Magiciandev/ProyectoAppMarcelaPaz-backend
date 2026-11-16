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
@Table(name = "prueba_final")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PruebaFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Una prueba final por objetivo de aprendizaje. */
    @Column(name = "objetivo_aprendizaje_id", nullable = false, unique = true)
    private Long objetivoAprendizajeId;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "criterios_puntuacion", columnDefinition = "TEXT")
    private String criteriosPuntuacion;

    /** Valor por defecto: el límite real de cada curso lo define el docente en la asignación (ms-progreso). */
    @Column(name = "limite_intentos", nullable = false)
    private Integer limiteIntentos;

    @Column(name = "estado", nullable = false, length = 15)
    private String estado;
}
