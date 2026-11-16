package com.mpaz.progreso.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.progreso.model.InformePedagogico;

@Repository
public interface InformeRepository extends JpaRepository<InformePedagogico, Long> {

    List<InformePedagogico> findByEstudianteIdOrderByFechaEmisionDesc(Long estudianteId);

    List<InformePedagogico> findByCursoIdOrderByFechaEmisionDesc(Long cursoId);
}
