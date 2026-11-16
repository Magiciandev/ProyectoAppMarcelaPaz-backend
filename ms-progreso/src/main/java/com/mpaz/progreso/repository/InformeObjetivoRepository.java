package com.mpaz.progreso.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.progreso.model.InformeObjetivo;

@Repository
public interface InformeObjetivoRepository extends JpaRepository<InformeObjetivo, Long> {

    List<InformeObjetivo> findByInformeId(Long informeId);
}
