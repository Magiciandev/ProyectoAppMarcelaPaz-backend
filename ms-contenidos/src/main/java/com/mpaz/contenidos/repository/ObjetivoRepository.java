package com.mpaz.contenidos.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.contenidos.model.ObjetivoAprendizaje;

@Repository
public interface ObjetivoRepository extends JpaRepository<ObjetivoAprendizaje, Long> {

    List<ObjetivoAprendizaje> findByUnidadIdOrderByOrdenAsc(Long unidadId);

    List<ObjetivoAprendizaje> findByIdIn(Collection<Long> ids);
}
