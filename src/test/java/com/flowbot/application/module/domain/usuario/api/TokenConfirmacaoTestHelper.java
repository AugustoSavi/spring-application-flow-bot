package com.flowbot.application.module.domain.usuario.api;

import com.flowbot.application.module.domain.usuario.TokenConfirmacao;

import java.time.LocalDateTime;

final class TokenConfirmacaoTestHelper {

    private TokenConfirmacaoTestHelper() {}

    static TokenConfirmacao criarExpirado(String email) {
        return TokenConfirmacao.criar(email, LocalDateTime.now().minusHours(1));
    }
}
