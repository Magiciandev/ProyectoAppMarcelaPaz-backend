package com.mpaz.evaluaciones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.evaluaciones.model.ActividadDesafio;

@Repository
public interface ActividadRepository extends JpaRepository<ActividadDesafio, Long> {

    List<ActividadDesafio> findByObjetivoAprendizajeIdOrderByOrdenAsc(Long objetivoAprendizajeId);
}
