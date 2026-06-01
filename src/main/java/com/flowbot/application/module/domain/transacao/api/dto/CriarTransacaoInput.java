package com.flowbot.application.module.domain.transacao.api.dto;

import java.math.BigDecimal;

public record CriarTransacaoInput(
        BigDecimal valor,
        String devedorNome,
        String devedorCPF,
        String descricaoSolicitacao
) {
}
