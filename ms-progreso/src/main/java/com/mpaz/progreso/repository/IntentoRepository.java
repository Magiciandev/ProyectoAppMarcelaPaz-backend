package com.mpaz.progreso.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.progreso.model.IntentoPrueba;

@Repository
public interface IntentoRepository extends JpaRepository<IntentoPrueba, Long> {

    Optional<IntentoPrueba> findByUuid(String uuid);

    long countByEstudianteIdAndObjetivoAprendizajeIdAndAceptadoTrueAndArchivadoFalse(Long estudianteId, Long objetivoAprendizajeId);

    List<IntentoPrueba> findByEstudianteIdAndAceptadoTrueAndArchivadoFalse(Long estudianteId);

    List<IntentoPrueba> findByEstudianteIdAndObjetivoAprendizajeIdAndAceptadoTrueAndArchivadoFalse(Long estudianteId, Long objetivoAprendizajeId);

    List<IntentoPrueba> findByEstudianteIdAndArchivadoFalseOrderByFechaHoraClienteAsc(Long estudianteId);

    List<IntentoPrueba> findByEstudianteIdAndObjetivoAprendizajeIdAndArchivadoFalseOrderByFechaHoraClienteAsc(Long estudianteId, Long objetivoAprendizajeId);

    List<IntentoPrueba> findByEstudianteIdAndArchivadoFalse(Long estudianteId);

    List<IntentoPrueba> findByEstudianteIdAndObjetivoAprendizajeIdAndArchivadoFalse(Long estudianteId, Long objetivoAprendizajeId);
}
