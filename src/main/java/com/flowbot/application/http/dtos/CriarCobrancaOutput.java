package com.flowbot.application.http.dtos;

public record CriarCobrancaOutput(
        String txId,
        String qrCode,
        String chave,
        String pixCopiaECola
) {
}
