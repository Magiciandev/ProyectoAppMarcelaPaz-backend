package com.mpaz.progreso.service;

import org.springframework.stereotype.Service;

import com.mpaz.progreso.exception.ForbiddenException;
import com.mpaz.progreso.exception.ResourceNotFoundException;
import com.mpaz.progreso.model.Curso;
import com.mpaz.progreso.model.Estudiante;
import com.mpaz.progreso.model.Rol;
import com.mpaz.progreso.repository.CursoRepository;
import com.mpaz.progreso.repository.EstudianteRepository;

/** Un docente solo trabaja con sus cursos; UTP puede consultarlos todos. */
@Service
public class AccesoService {

    private final CursoRepository cursoRepository;
    private final EstudianteRepository estudianteRepository;

    public AccesoService(CursoRepository cursoRepository, EstudianteRepository estudianteRepository) {
        this.cursoRepository = cursoRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public Curso cursoPermitido(Long cursoId, Rol rol, Long usuarioId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
        if (rol == Rol.DOCENTE && !curso.getDocenteId().equals(usuarioId)) {
            throw new ForbiddenException("Ese curso no es tuyo");
        }
        return curso;
    }

    public Estudiante estudiantePermitido(Long estudianteId, Rol rol, Long usuarioId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        cursoPermitido(estudiante.getCursoId(), rol, usuarioId);
        return estudiante;
    }
}
