package com.mpaz.progreso.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.progreso.model.Asignacion;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    List<Asignacion> findByCursoIdAndDisponibleTrue(Long cursoId);

    List<Asignacion> findByEstudianteIdAndDisponibleTrue(Long estudianteId);

    List<Asignacion> findByCursoId(Long cursoId);

    List<Asignacion> findByEstudianteIdIn(Collection<Long> estudianteIds);

    Optional<Asignacion> findByObjetivoAprendizajeIdAndCursoId(Long objetivoAprendizajeId, Long cursoId);

    Optional<Asignacion> findByObjetivoAprendizajeIdAndEstudianteId(Long objetivoAprendizajeId, Long estudianteId);
}
