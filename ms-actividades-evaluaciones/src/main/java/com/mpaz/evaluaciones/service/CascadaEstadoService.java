package com.mpaz.evaluaciones.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.evaluaciones.model.ActividadDesafio;
import com.mpaz.evaluaciones.model.Estado;
import com.mpaz.evaluaciones.repository.ActividadRepository;
import com.mpaz.evaluaciones.repository.PruebaRepository;

/** Aplica a las actividades y a la prueba final de un objetivo el estado que ms-admin decidió para él. */
@Service
public class CascadaEstadoService {

    private final ActividadRepository actividadRepository;
    private final PruebaRepository pruebaRepository;

    public CascadaEstadoService(ActividadRepository actividadRepository, PruebaRepository pruebaRepository) {
        this.actividadRepository = actividadRepository;
        this.pruebaRepository = pruebaRepository;
    }

    /** Es válido que el objetivo no tenga actividades ni prueba: en ese caso no hay nada que cambiar. */
    @Transactional
    public void fijarEstado(Long objetivoId, String estadoNuevo) {
        Estado estado = Estado.de(estadoNuevo);
        List<ActividadDesafio> actividades = actividadRepository
                .findByObjetivoAprendizajeIdOrderByOrdenAsc(objetivoId);
        actividades.forEach(a -> a.setEstado(estado.name()));
        actividadRepository.saveAll(actividades);
        pruebaRepository.findByObjetivoAprendizajeId(objetivoId).ifPresent(p -> {
            p.setEstado(estado.name());
            pruebaRepository.save(p);
        });
    }
}
