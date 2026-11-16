package com.mpaz.progreso.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.progreso.config.Reloj;
import com.mpaz.progreso.dto.ReinicioDTO;
import com.mpaz.progreso.dto.ReinicioRequest;
import com.mpaz.progreso.model.Estudiante;
import com.mpaz.progreso.model.IntentoPrueba;
import com.mpaz.progreso.model.Progreso;
import com.mpaz.progreso.model.ReinicioProgreso;
import com.mpaz.progreso.model.Rol;
import com.mpaz.progreso.repository.IntentoRepository;
import com.mpaz.progreso.repository.ProgresoRepository;
import com.mpaz.progreso.repository.ReinicioRepository;

/**
 * El docente reinicia el avance de un estudiante (de un objetivo o de todos). No se borra nada: los intentos se
 * archivan y queda el registro del reinicio. Lo que el dispositivo haya hecho antes de esa fecha ya no cuenta.
 */
@Service
public class ReinicioService {

    private final IntentoRepository intentoRepository;
    private final ProgresoRepository progresoRepository;
    private final ReinicioRepository reinicioRepository;
    private final AccesoService accesoService;
    private final Reloj reloj;

    public ReinicioService(IntentoRepository intentoRepository, ProgresoRepository progresoRepository,
                           ReinicioRepository reinicioRepository, AccesoService accesoService, Reloj reloj) {
        this.intentoRepository = intentoRepository;
        this.progresoRepository = progresoRepository;
        this.reinicioRepository = reinicioRepository;
        this.accesoService = accesoService;
        this.reloj = reloj;
    }

    @Transactional
    public ReinicioDTO reiniciar(Long docenteId, Long estudianteId, ReinicioRequest request) {
        Estudiante estudiante = accesoService.estudiantePermitido(estudianteId, Rol.DOCENTE, docenteId);
        Long objetivoId = request.getObjetivoId();
        Instant ahora = reloj.ahora();

        List<IntentoPrueba> intentos = objetivoId == null
                ? intentoRepository.findByEstudianteIdAndArchivadoFalse(estudiante.getUsuarioId())
                : intentoRepository.findByEstudianteIdAndObjetivoAprendizajeIdAndArchivadoFalse(
                        estudiante.getUsuarioId(), objetivoId);
        intentos.forEach(i -> i.setArchivado(true));
        intentoRepository.saveAll(intentos);

        List<Progreso> progresos = objetivoId == null
                ? progresoRepository.findByEstudianteId(estudiante.getUsuarioId())
                : progresoRepository.findByEstudianteIdAndObjetivoAprendizajeId(estudiante.getUsuarioId(), objetivoId)
                        .map(List::of).orElse(List.of());
        for (Progreso p : progresos) {
            p.setContenidosCompletados(0);
            p.setActividadesCompletadas(0);
            p.setDesafiosCompletados(0);
            p.setPorcentajeAvance(0);
            p.setMarcaTiempo(ahora);
        }
        progresoRepository.saveAll(progresos);

        reinicioRepository.save(new ReinicioProgreso(null, estudiante.getUsuarioId(), objetivoId, docenteId, ahora,
                request.getMotivo()));
        return new ReinicioDTO(ahora, objetivoId);
    }
}
