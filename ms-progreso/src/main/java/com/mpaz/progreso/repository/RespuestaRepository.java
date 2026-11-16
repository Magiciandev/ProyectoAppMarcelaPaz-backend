package com.mpaz.progreso.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.progreso.model.RespuestaEstudiante;

@Repository
public interface RespuestaRepository extends JpaRepository<RespuestaEstudiante, Long> {

    boolean existsByUuid(String uuid);

    List<RespuestaEstudiante> findByIntentoIdIn(Collection<Long> intentoIds);

    List<RespuestaEstudiante> findByIntentoId(Long intentoId);
}
