package com.mpaz.contenidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.contenidos.model.Unidad;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Long> {

    List<Unidad> findAllByOrderByOrdenAsc();

    List<Unidad> findByAsignaturaIdOrderByOrdenAsc(Long asignaturaId);
}
