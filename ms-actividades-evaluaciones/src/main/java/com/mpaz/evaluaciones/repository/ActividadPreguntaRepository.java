package com.mpaz.evaluaciones.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.evaluaciones.model.ActividadPregunta;

@Repository
public interface ActividadPreguntaRepository extends JpaRepository<ActividadPregunta, Long> {

    List<ActividadPregunta> findByActividadIdOrderByOrdenAsc(Long actividadId);

    List<ActividadPregunta> findByActividadIdIn(Collection<Long> actividadIds);

    List<ActividadPregunta> findByPreguntaId(Long preguntaId);

    void deleteByActividadId(Long actividadId);
}
