package com.mpaz.progreso.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.progreso.model.InformeRespuesta;

@Repository
public interface InformeRespuestaRepository extends JpaRepository<InformeRespuesta, Long> {

    List<InformeRespuesta> findByInformeId(Long informeId);
}
