package com.flowbot.application.module.domain.financeiro.assinaturas.api.dto;

import java.util.List;

public record ExpirarPlanosVencidosResultDto(int totalAtualizado, List<String> tenantsAfetados) {
}
