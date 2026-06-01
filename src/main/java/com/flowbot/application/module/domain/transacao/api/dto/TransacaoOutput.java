package com.flowbot.application.module.domain.transacao.api.dto;

import java.math.BigDecimal;

public record TransacaoOutput(
        String id,
        String externalReference,
        BigDecimal valor,
        String devedorNome,
        String devedorCPF,
        String descricaoSolicitacao,
        String status,
        String createdAt,
        String qrCode,
        String pixCopiaECola
) {
}
