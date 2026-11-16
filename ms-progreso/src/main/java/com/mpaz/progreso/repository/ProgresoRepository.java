package com.mpaz.progreso.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.progreso.model.Progreso;

@Repository
public interface ProgresoRepository extends JpaRepository<Progreso, Long> {

    List<Progreso> findByEstudianteId(Long estudianteId);

    List<Progreso> findByEstudianteIdIn(Collection<Long> estudianteIds);

    Optional<Progreso> findByEstudianteIdAndObjetivoAprendizajeId(Long estudianteId, Long objetivoAprendizajeId);
}
