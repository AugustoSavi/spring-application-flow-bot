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

        var transacao = repository
                .findByExternalReferenceAndResourceOwner(input.externalRef(), input.clientID())
                .orElseThrow(() -> new ValidationException("Transação não encontrada para o cliente informado"));

        transacao.atualizarStatus(StatusTransacao.PAGAMENTO_EFETUADO);
        repository.save(transacao);
    }

    private void validar(ConfirmacaoTransacaoWebhookInput input) {
        if (Objects.isNull(input)) {
            throw new ValidationException("Payload do webhook não pode ser nulo");
        }
        if (Objects.isNull(input.clientID()) || input.clientID().isBlank()) {
            throw new ValidationException("clientID é obrigatório");
        }
        if (Objects.isNull(input.externalRef()) || input.externalRef().isBlank()) {
            throw new ValidationException("externalRef é obrigatório");
        }
    }
}
