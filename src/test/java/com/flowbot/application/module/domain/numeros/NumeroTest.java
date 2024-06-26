package com.flowbot.application.module.domain.numeros;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Numero Object Test")
class NumeroTest {

    @Test
    public void deveCriarObjetoCorretamente() {
        var numero = new Numero(null, "nick", null, null, "558890809809", null);
        assertEquals(numero.getNick(), "nick");
        assertEquals(numero.getNumero(), "558890809809");
        assertNull(numero.getId());
        assertEquals(StatusNumero.CRIADO, numero.getStatusNumero());
        assertNotNull(numero.getDataCriacao());
        assertNull(numero.getWhatsappInternalId());
    }

    @Test
    void deveAtualizarCorretamente() {
        var numero = new Numero(null, "nick", null, StatusNumero.BANIDO, "558890809809", "foiwehfow");
        numero.atualizarNumero("558890809809", "internalId");

        assertEquals(numero.getNick(), "internalId");
        assertEquals(numero.getNumero(), "558890809809");
        assertNull(numero.getId());
        assertEquals(StatusNumero.PENDENTE, numero.getStatusNumero());
        assertNotNull(numero.getDataCriacao());
        assertNull(numero.getWhatsappInternalId());
    }

}