package com.mpaz.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpaz.admin.model.HistorialAprobacion;

@Repository
public interface HistorialRepository extends JpaRepository<HistorialAprobacion, Long> {

    List<HistorialAprobacion> findByEntidadAndEntidadIdOrderByFechaDesc(String entidad, Long entidadId);
}
