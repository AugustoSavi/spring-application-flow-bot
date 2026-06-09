package com.flowbot.application.http.dtos;

import java.math.BigDecimal;

public record CriarCobrancaInput(
        BigDecimal valor,
        String devedorNome,
        String devedorCPF,
        String descricaoSolicitacao,
        String externalReference
) {
}
