package com.mpaz.contenidos.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.mpaz.contenidos.exception.ForbiddenException;

class RolTest {

    @Test
    void utpVeTodosLosEstados() {
        for (Estado estado : Estado.values()) {
            assertThat(Rol.UTP.vee(estado)).isTrue();
        }
    }

    @Test
    void docenteSoloVeAutorizadoYPublicado() {
        assertThat(Rol.DOCENTE.vee(Estado.AUTORIZADO)).isTrue();
        assertThat(Rol.DOCENTE.vee(Estado.PUBLICADO)).isTrue();
        assertThat(Rol.DOCENTE.vee(Estado.BORRADOR)).isFalse();
        assertThat(Rol.DOCENTE.vee(Estado.EN_REVISION)).isFalse();
        assertThat(Rol.DOCENTE.vee(Estado.DESHABILITADO)).isFalse();
    }

    @Test
    void estudianteSoloVePublicado() {
        assertThat(Rol.ESTUDIANTE.vee(Estado.PUBLICADO)).isTrue();
        assertThat(Rol.ESTUDIANTE.vee(Estado.AUTORIZADO)).isFalse();
    }

    @Test
    void rolDesconocidoEsProhibido() {
        assertThatThrownBy(() -> Rol.de("INVITADO")).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> Rol.de(null)).isInstanceOf(ForbiddenException.class);
    }
}
