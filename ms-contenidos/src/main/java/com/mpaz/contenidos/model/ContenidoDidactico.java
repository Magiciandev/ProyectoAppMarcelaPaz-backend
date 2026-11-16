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
@Table(name = "contenido_didactico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoDidactico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "objetivo_id", nullable = false)
    private Long objetivoId;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "explicacion", nullable = false, columnDefinition = "TEXT")
    private String explicacion;

    @Column(name = "instrucciones", columnDefinition = "TEXT")
    private String instrucciones;

    @Column(name = "ejemplos_guiados", columnDefinition = "TEXT")
    private String ejemplosGuiados;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(name = "archivo_audio", length = 255)
    private String archivoAudio;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    /** Autoría del material (regla del caso: autoría y licenciamiento validados). */
    @Column(name = "responsable_origen", nullable = false, length = 120)
    private String responsableOrigen;

    @Column(name = "licencia", nullable = false, length = 80)
    private String licencia;
}
