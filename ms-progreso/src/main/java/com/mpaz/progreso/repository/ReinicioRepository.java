package com.mpaz.progreso.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.progreso.model.ReinicioProgreso;

@Repository
public interface ReinicioRepository extends JpaRepository<ReinicioProgreso, Long> {

    List<ReinicioProgreso> findByEstudianteIdOrderByFechaDesc(Long estudianteId);
}
