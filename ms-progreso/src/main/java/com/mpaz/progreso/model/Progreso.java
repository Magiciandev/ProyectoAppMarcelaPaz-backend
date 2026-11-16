package com.mpaz.progreso.model;

import java.time.Instant;

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
@Table(name = "progreso", uniqueConstraints = @UniqueConstraint(columnNames = {"estudiante_id", "objetivo_aprendizaje_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Progreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estudiante_id", nullable = false)
    private Long estudianteId;

    @Column(name = "objetivo_aprendizaje_id", nullable = false)
    private Long objetivoAprendizajeId;

    @Column(name = "contenidos_completados", nullable = false)
    private Integer contenidosCompletados;

    @Column(name = "actividades_completadas", nullable = false)
    private Integer actividadesCompletadas;

    @Column(name = "desafios_completados", nullable = false)
    private Integer desafiosCompletados;

    @Column(name = "porcentaje_avance", nullable = false)
    private Integer porcentajeAvance;

    @Column(name = "marca_tiempo", nullable = false)
    private Instant marcaTiempo;
}
