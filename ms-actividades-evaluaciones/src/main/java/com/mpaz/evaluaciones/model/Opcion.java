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
@Table(name = "opcion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Opcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pregunta_id", nullable = false)
    private Long preguntaId;

    @Column(name = "texto", nullable = false, length = 200)
    private String texto;

    /** SELECCION y COMPLETACION. */
    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta;

    /** SECUENCIA: lugar correcto (1, 2, 3...). */
    @Column(name = "posicion_correcta")
    private Integer posicionCorrecta;

    /** ASOCIACION: dos opciones con la misma clave son pareja. */
    @Column(name = "par_clave", length = 20)
    private String parClave;

    /** Orden en que se muestra. */
    @Column(name = "orden", nullable = false)
    private Integer orden;
}
