package com.mpaz.evaluaciones.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.evaluaciones.model.Pregunta;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    List<Pregunta> findByObjetivoAprendizajeIdOrderByIdAsc(Long objetivoAprendizajeId);

    List<Pregunta> findByIdIn(Collection<Long> ids);
}
