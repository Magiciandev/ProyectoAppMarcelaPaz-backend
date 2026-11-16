package com.mpaz.contenidos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mpaz.contenidos.dto.ContenidoRequest;
import com.mpaz.contenidos.dto.ObjetivoDTO;
import com.mpaz.contenidos.dto.ObjetivoRequest;
import com.mpaz.contenidos.dto.UnidadDTO;
import com.mpaz.contenidos.exception.BadRequestException;
import com.mpaz.contenidos.exception.ConflictException;
import com.mpaz.contenidos.exception.ResourceNotFoundException;
import com.mpaz.contenidos.model.Estado;
import com.mpaz.contenidos.model.ObjetivoAprendizaje;
import com.mpaz.contenidos.model.Rol;
import com.mpaz.contenidos.model.Unidad;
import com.mpaz.contenidos.repository.AsignaturaRepository;
import com.mpaz.contenidos.repository.ContenidoRepository;
import com.mpaz.contenidos.repository.ObjetivoRepository;
import com.mpaz.contenidos.repository.UnidadRepository;

class ContenidosServiceTest {

    private AsignaturaRepository asignaturas;
    private UnidadRepository unidades;
    private ObjetivoRepository objetivos;
    private ContenidoRepository contenidos;
    private ContenidosService service;

    @BeforeEach
    void preparar() {
        asignaturas = mock(AsignaturaRepository.class);
        unidades = mock(UnidadRepository.class);
        objetivos = mock(ObjetivoRepository.class);
        contenidos = mock(ContenidoRepository.class);
        service = new ContenidosService(asignaturas, unidades, objetivos, contenidos);
    }

    private ObjetivoAprendizaje objetivo(long id, long unidadId, Estado estado) {
        return new ObjetivoAprendizaje(id, unidadId, "OA-" + id, "Descripción", "Eje", null, 1, estado.name());
    }

    private Unidad unidad(long id, Estado estado) {
        return new Unidad(id, 1L, "Unidad " + id, 1, estado.name(), null, null, 1L, Instant.now());
    }

    @Test
    void docenteNoVeUnObjetivoEnBorrador() {
        when(objetivos.findById(1L)).thenReturn(Optional.of(objetivo(1, 10, Estado.BORRADOR)));

        assertThatThrownBy(() -> service.obtenerObjetivo(Rol.DOCENTE, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void utpVeUnObjetivoEnBorrador() {
        when(objetivos.findById(1L)).thenReturn(Optional.of(objetivo(1, 10, Estado.BORRADOR)));

        ObjetivoDTO dto = service.obtenerObjetivo(Rol.UTP, 1L);

        assertThat(dto.getEstado()).isEqualTo("BORRADOR");
    }

    @Test
    void estudianteVeUnObjetivoPublicado() {
        when(objetivos.findById(2L)).thenReturn(Optional.of(objetivo(2, 10, Estado.PUBLICADO)));

        assertThat(service.obtenerObjetivo(Rol.ESTUDIANTE, 2L).getId()).isEqualTo(2L);
    }

    @Test
    void listaDeUnidadesSeFiltraPorEstadoYRol() {
        when(unidades.findAllByOrderByOrdenAsc()).thenReturn(List.of(
                unidad(1, Estado.BORRADOR), unidad(2, Estado.AUTORIZADO), unidad(3, Estado.PUBLICADO)));

        List<UnidadDTO> paraDocente = service.listarUnidades(Rol.DOCENTE, null);
        List<UnidadDTO> paraEstudiante = service.listarUnidades(Rol.ESTUDIANTE, null);
        List<UnidadDTO> paraUtp = service.listarUnidades(Rol.UTP, null);

        assertThat(paraDocente).extracting(UnidadDTO::getId).containsExactly(2L, 3L);
        assertThat(paraEstudiante).extracting(UnidadDTO::getId).containsExactly(3L);
        assertThat(paraUtp).hasSize(3);
    }

    @Test
    void listarObjetivosSinFiltrosEsPeticionInvalida() {
        assertThatThrownBy(() -> service.listarObjetivos(Rol.UTP, null, null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void noSePuedeEditarUnObjetivoFueraDeBorrador() {
        when(objetivos.findById(5L)).thenReturn(Optional.of(objetivo(5, 10, Estado.EN_REVISION)));

        ObjetivoRequest request = new ObjetivoRequest("OA-5", "Nueva descripción", null, null, 1);

        assertThatThrownBy(() -> service.actualizarObjetivo(5L, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void noSePuedeAgregarContenidoFueraDeBorrador() {
        when(objetivos.findById(5L)).thenReturn(Optional.of(objetivo(5, 10, Estado.PUBLICADO)));

        ContenidoRequest request = new ContenidoRequest("Título", "Explicación", null, null, null, null, 1,
                "Autor", "Propio");

        assertThatThrownBy(() -> service.crearContenido(5L, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void fijarEstadoRecalculaElEstadoDeLaUnidad() {
        ObjetivoAprendizaje oa = objetivo(7, 10, Estado.AUTORIZADO);
        Unidad unidad = unidad(10, Estado.AUTORIZADO);
        when(objetivos.findById(7L)).thenReturn(Optional.of(oa));
        when(unidades.findById(10L)).thenReturn(Optional.of(unidad));
        when(objetivos.findByUnidadIdOrderByOrdenAsc(10L)).thenReturn(List.of(oa));

        ObjetivoDTO dto = service.fijarEstadoObjetivo(7L, "PUBLICADO");

        assertThat(dto.getEstado()).isEqualTo("PUBLICADO");
        assertThat(unidad.getEstado()).isEqualTo("PUBLICADO");
    }

    @Test
    void fijarEstadoInvalidoEsPeticionInvalida() {
        assertThatThrownBy(() -> service.fijarEstadoObjetivo(7L, "VOLANDO"))
                .isInstanceOf(BadRequestException.class);
    }
}
