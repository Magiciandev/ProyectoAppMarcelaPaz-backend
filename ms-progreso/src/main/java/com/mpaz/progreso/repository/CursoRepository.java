package com.mpaz.progreso.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.progreso.model.Curso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    List<Curso> findByDocenteIdOrderByNombreAsc(Long docenteId);

    List<Curso> findAllByOrderByNombreAsc();
}
