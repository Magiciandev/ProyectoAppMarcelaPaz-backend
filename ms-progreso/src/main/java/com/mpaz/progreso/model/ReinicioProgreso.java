package com.mpaz.progreso.model;

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
@Table(name = "reinicio_progreso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReinicioProgreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estudiante_id", nullable = false)
    private Long estudianteId;

    /** Nulo = todos los objetivos. */
    @Column(name = "objetivo_aprendizaje_id")
    private Long objetivoAprendizajeId;

    @Column(name = "docente_id", nullable = false)
    private Long docenteId;

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Column(name = "motivo", length = 255)
    private String motivo;
}
