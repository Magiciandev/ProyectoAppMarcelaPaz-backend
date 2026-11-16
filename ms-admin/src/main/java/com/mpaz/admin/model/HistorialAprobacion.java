package com.mpaz.admin.model;

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
@Table(name = "historial_aprobacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialAprobacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Por ahora siempre OBJETIVO. */
    @Column(name = "entidad", nullable = false, length = 12)
    private String entidad;

    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    /** Id del usuario UTP en ms-auth. */
    @Column(name = "responsable_utp_id", nullable = false)
    private Long responsableUtpId;

    @Column(name = "accion", nullable = false, length = 20)
    private String accion;

    @Column(name = "estado_anterior", nullable = false, length = 15)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 15)
    private String estadoNuevo;

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Column(name = "observacion", length = 255)
    private String observacion;
}
