package com.mpaz.evaluaciones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.evaluaciones.model.PruebaPregunta;

@Repository
public interface PruebaPreguntaRepository extends JpaRepository<PruebaPregunta, Long> {

    List<PruebaPregunta> findByPruebaIdOrderByOrdenEnPruebaAsc(Long pruebaId);

    List<PruebaPregunta> findByPreguntaId(Long preguntaId);

    void deleteByPruebaId(Long pruebaId);
}
