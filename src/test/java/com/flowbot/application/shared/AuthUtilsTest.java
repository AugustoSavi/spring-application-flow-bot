package com.flowbot.application.shared;

import com.flowbot.application.context.TenantThreads;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthUtilsTest {

    @AfterEach
    void clearTenant() {
        TenantThreads.clear();
    }

    @Test
    @DisplayName("Email normal deriva tenant como primeiros4 + últimos4 do valor processado")
    void setTenantFromEmail_emailNormal() {
        var tenant = AuthUtils.setTenantFromEmail("john@doe.io");
        assertEquals("johnoeio", tenant);
        assertEquals("johnoeio", TenantThreads.getTenantId());
    }

    @Test
    @DisplayName("Email com string processada menor que 4 caracteres não lança exceção")
    void setTenantFromEmail_emailCurto_naoLancaExcecao() {
        assertDoesNotThrow(() -> AuthUtils.setTenantFromEmail("a@b"));
    }

    @Test
    @DisplayName("Email com string processada menor que 4 caracteres usa a string completa como tenant")
    void setTenantFromEmail_emailCurto_usaStringCompleta() {
        var tenant = AuthUtils.setTenantFromEmail("a@b");
        assertEquals("a@b", tenant);
        assertEquals("a@b", TenantThreads.getTenantId());
    }

    @Test
    @DisplayName("Email com exatamente 4 caracteres processados não lança exceção")
    void setTenantFromEmail_emailExatamente4Chars() {
        assertDoesNotThrow(() -> AuthUtils.setTenantFromEmail("a@bc"));
    }
}
