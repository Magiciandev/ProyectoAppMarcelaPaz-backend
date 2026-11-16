package com.mpaz.evaluaciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "actividad_pregunta", uniqueConstraints = @UniqueConstraint(columnNames = {"actividad_id", "pregunta_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actividad_id", nullable = false)
    private Long actividadId;

    @Column(name = "pregunta_id", nullable = false)
    private Long preguntaId;

    @Column(name = "orden", nullable = false)
    private Integer orden;
}
