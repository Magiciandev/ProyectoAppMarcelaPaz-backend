package com.mpaz.evaluaciones.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.evaluaciones.model.PruebaFinal;

@Repository
public interface PruebaRepository extends JpaRepository<PruebaFinal, Long> {

    Optional<PruebaFinal> findByObjetivoAprendizajeId(Long objetivoAprendizajeId);
}
