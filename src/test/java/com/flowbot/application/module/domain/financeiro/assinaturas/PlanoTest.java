package com.flowbot.application.module.domain.financeiro.assinaturas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.flowbot.application.TestUtils.compareIgnoringSecondsAndMillis;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Plano Object Test")
class PlanoTest {

    @Test
    void deveCriarComPlanoPadrao() {
        final var email = "xxxx@xxx.com";
        final var plano = PeriodoPlano.ANUAL;
        final var planoObject = Plano.criarPlanoPadrao(email, plano);
        assertNotNull(planoObject);
        assertNull(planoObject.getId());
        assertTrue(compareIgnoringSecondsAndMillis(LocalDateTime.now(), planoObject.getDataCriacao()));
        assertTrue(compareIgnoringSecondsAndMillis(LocalDateTime.now().plusYears(1), planoObject.getFinalizaEm()));

        assertTrue(planoObject.getAtivo());
        assertEquals(email, planoObject.getUsuario().nick());
        assertEquals(email, planoObject.getUsuario().email());
        assertEquals(plano, planoObject.getPeriodoPlano());
    }

    @Test
    void deveAplicarAsValidacoes() {
        final var plano = PeriodoPlano.ANUAL;
        assertThrows(IllegalArgumentException.class, () -> Plano.criarPlanoPadrao(null, plano));

        assertThrows(IllegalArgumentException.class, () -> Plano.criarPlanoPadrao("", plano));

        assertThrows(IllegalArgumentException.class, () -> Plano.criarPlanoPadrao("null", null));
    }

}