package com.mpaz.progreso.model;

import java.math.BigDecimal;
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
@Table(name = "intento_prueba")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntentoPrueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Lo crea el dispositivo; hace los envíos idempotentes. */
    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "estudiante_id", nullable = false)
    private Long estudianteId;

    @Column(name = "prueba_id", nullable = false)
    private Long pruebaId;

    @Column(name = "objetivo_aprendizaje_id", nullable = false)
    private Long objetivoAprendizajeId;

    @Column(name = "numero_intento", nullable = false)
    private Integer numeroIntento;

    @Column(name = "puntaje_obtenido", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeObtenido;

    @Column(name = "puntaje_maximo", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeMaximo;

    @Column(name = "porcentaje", nullable = false)
    private Integer porcentaje;

    /** Fecha del dispositivo: la que ordena y valida los intentos. */
    @Column(name = "fecha_hora_cliente", nullable = false)
    private Instant fechaHoraCliente;

    @Column(name = "fecha_recepcion", nullable = false)
    private Instant fechaRecepcion;

    @Column(name = "aceptado", nullable = false)
    private boolean aceptado;

    @Column(name = "motivo_rechazo", length = 30)
    private String motivoRechazo;

    /** Se marca al reiniciar el progreso; no se borra. */
    @Column(name = "archivado", nullable = false)
    private boolean archivado;
}
