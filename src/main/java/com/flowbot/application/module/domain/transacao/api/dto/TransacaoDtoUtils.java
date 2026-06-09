package com.flowbot.application.module.domain.transacao.api.dto;

import com.flowbot.application.module.domain.transacao.Transacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static com.flowbot.application.utils.Utils.calculateElapsedTime;

public final class TransacaoDtoUtils {

    public static Page<TransacaoOutput> toDto(Page<Transacao> page) {
        Pageable pageable = page.getPageable();
        var content = page.getContent().stream().map(TransacaoDtoUtils::toOutput).toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public static TransacaoOutput toOutput(Transacao transacao) {
        return new TransacaoOutput(
                transacao.getId(),
                transacao.getExternalReference(),
                transacao.getValor(),
                transacao.getDevedorNome(),
                transacao.getDevedorCPF(),
                transacao.getDescricaoSolicitacao(),
                transacao.getStatus().toString(),
                calculateElapsedTime(transacao.getDataCriacao()),
                transacao.getQrCode(),
                transacao.getPixCopiaECola()
        );
    }
}
