package com.mpaz.contenidos.model;

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
@Table(name = "objetivo_aprendizaje")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjetivoAprendizaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unidad_id", nullable = false)
    private Long unidadId;

    @Column(name = "codigo", nullable = false, length = 12)
    private String codigo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "eje_curricular", length = 60)
    private String ejeCurricular;

    @Column(name = "texto_referencia", columnDefinition = "TEXT")
    private String textoReferencia;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "estado", nullable = false, length = 15)
    private String estado;
}
