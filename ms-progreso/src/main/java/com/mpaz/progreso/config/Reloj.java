package com.mpaz.progreso.config;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Hora actual y conversión a fecha de calendario en la zona del colegio. Se puede reemplazar en las pruebas. */
@Component
public class Reloj {

    private final Clock clock;
    private final ZoneId zona;

    public Reloj(Clock clock, @Value("${mpaz.zona-horaria:America/Santiago}") String zona) {
        this.clock = clock;
        this.zona = ZoneId.of(zona);
    }

    public Instant ahora() {
        return clock.instant();
    }

    public LocalDate hoy() {
        return LocalDate.ofInstant(clock.instant(), zona);
    }

    public LocalDate fechaDe(Instant momento) {
        return LocalDate.ofInstant(momento, zona);
    }
}
