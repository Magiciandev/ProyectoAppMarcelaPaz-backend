package com.mpaz.evaluaciones.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.evaluaciones.model.Opcion;

@Repository
public interface OpcionRepository extends JpaRepository<Opcion, Long> {

    List<Opcion> findByPreguntaIdOrderByOrdenAsc(Long preguntaId);

    List<Opcion> findByPreguntaIdInOrderByOrdenAsc(Collection<Long> preguntaIds);

    void deleteByPreguntaId(Long preguntaId);
}
