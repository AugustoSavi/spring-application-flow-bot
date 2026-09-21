package com.flowbot.application.module.domain.transacao.useCase;

import com.flowbot.application.module.domain.transacao.StatusTransacao;
import com.flowbot.application.module.domain.transacao.TransacaoMongoDbRepository;
import com.flowbot.application.module.domain.transacao.api.dto.ConfirmacaoTransacaoWebhookInput;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ConfirmarPagamentoTransacaoUseCase {

    private final TransacaoMongoDbRepository repository;

    public ConfirmarPagamentoTransacaoUseCase(TransacaoMongoDbRepository repository) {
        this.repository = repository;
    }

    public void execute(final ConfirmacaoTransacaoWebhookInput input) {
        validar(input);

        // O externalReference e um UUID gerado pelo servidor na criacao da
        // transacao, portanto e globalmente unico e identifica a transacao sem
        // ambiguidade. O webhook chega de um sistema externo (nao autenticado),
        // entao nao temos o resourceOwner do usuario para combinar na busca.
        var transacao = repository
                .findByExternalReference(input.externalRef())
                .orElseThrow(() -> new ValidationException("Transação não encontrada para o externalRef informado"));

        transacao.atualizarStatus(StatusTransacao.PAGAMENTO_EFETUADO);
        repository.save(transacao);
    }

    private void validar(ConfirmacaoTransacaoWebhookInput input) {
        if (Objects.isNull(input)) {
            throw new ValidationException("Payload do webhook não pode ser nulo");
        }
        if (Objects.isNull(input.externalRef()) || input.externalRef().isBlank()) {
            throw new ValidationException("externalRef é obrigatório");
        }
    }
}
