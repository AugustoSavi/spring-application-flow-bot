package com.flowbot.application.module.domain.financeiro.assinaturas.api.dto;

public record CriarPlanoInputDto(
        String email,
        String periodoPlano,
        Boolean gratuito
) {
}
