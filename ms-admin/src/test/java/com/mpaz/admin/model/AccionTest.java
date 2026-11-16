package com.mpaz.admin.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.mpaz.admin.exception.BadRequestException;

class AccionTest {

    @Test
    void cadaAccionSoloSePermiteDesdeSuEstado() {
        assertThat(Accion.ENVIAR_REVISION.permiteDesde(Estado.BORRADOR)).isTrue();
        assertThat(Accion.ENVIAR_REVISION.permiteDesde(Estado.PUBLICADO)).isFalse();
        assertThat(Accion.AUTORIZAR.permiteDesde(Estado.EN_REVISION)).isTrue();
        assertThat(Accion.AUTORIZAR.permiteDesde(Estado.BORRADOR)).isFalse();
        assertThat(Accion.PUBLICAR.permiteDesde(Estado.AUTORIZADO)).isTrue();
        assertThat(Accion.PUBLICAR.permiteDesde(Estado.EN_REVISION)).isFalse();
        assertThat(Accion.DESHABILITAR.permiteDesde(Estado.PUBLICADO)).isTrue();
        assertThat(Accion.REABRIR.permiteDesde(Estado.DESHABILITADO)).isTrue();
    }

    @Test
    void lasTransicionesLlevanAlEstadoEsperado() {
        assertThat(Accion.ENVIAR_REVISION.hasta()).isEqualTo(Estado.EN_REVISION);
        assertThat(Accion.AUTORIZAR.hasta()).isEqualTo(Estado.AUTORIZADO);
        assertThat(Accion.RECHAZAR.hasta()).isEqualTo(Estado.BORRADOR);
        assertThat(Accion.PUBLICAR.hasta()).isEqualTo(Estado.PUBLICADO);
        assertThat(Accion.DESHABILITAR.hasta()).isEqualTo(Estado.DESHABILITADO);
        assertThat(Accion.REABRIR.hasta()).isEqualTo(Estado.BORRADOR);
    }

    @Test
    void interpretaElTextoSinImportarMayusculas() {
        assertThat(Accion.de(" autorizar ")).isEqualTo(Accion.AUTORIZAR);
    }

    @Test
    void accionDesconocidaEsPeticionInvalida() {
        assertThatThrownBy(() -> Accion.de("VOLAR")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> Accion.de(null)).isInstanceOf(BadRequestException.class);
    }
}
