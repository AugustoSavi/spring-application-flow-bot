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
        assertTrue(planoObject.getGratuito());
        assertEquals(email, planoObject.getUsuario().nick());
        assertEquals(email, planoObject.getUsuario().email());
        assertEquals(plano, planoObject.getPeriodoPlano());
    }

    @Test
    @DisplayName("Plano sem campo gratuito no documento deve deserializar como false")
    void deveRetornarGratuitoFalsePorPadrao() {
        assertFalse(new Plano().getGratuito());
    }

    @Test
    @DisplayName("Ao processar reembolso o plano deve ser marcado como gratuito")
    void deveMarcarComoGratuitoAoProcessarReembolso() {
        var plano = Plano.criarPlano("pago@email.com", PeriodoPlano.MENSAL, false);
        assertFalse(plano.getGratuito());

        plano.processarReembolso();

        assertTrue(plano.getGratuito());
        assertTrue(compareIgnoringSecondsAndMillis(LocalDateTime.now().plusDays(5), plano.getFinalizaEm()));
    }

}
