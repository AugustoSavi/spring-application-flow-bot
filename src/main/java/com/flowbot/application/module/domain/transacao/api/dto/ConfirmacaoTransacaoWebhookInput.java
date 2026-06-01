package com.flowbot.application.module.domain.transacao.api.dto;

public record ConfirmacaoTransacaoWebhookInput(
        String clientID,
        String externalRef,
        String webhookUrl
) {
}
