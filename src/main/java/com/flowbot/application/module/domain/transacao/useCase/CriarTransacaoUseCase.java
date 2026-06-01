package com.flowbot.application.module.domain.transacao.useCase;

import com.flowbot.application.http.PixApi;
import com.flowbot.application.http.dtos.CriarCobrancaInput;
import com.flowbot.application.module.domain.transacao.Transacao;
import com.flowbot.application.module.domain.transacao.TransacaoMongoDbRepository;
import com.flowbot.application.module.domain.transacao.api.dto.CriarTransacaoInput;
import com.flowbot.application.shared.AuthUtils;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
public class CriarTransacaoUseCase {

    private final TransacaoMongoDbRepository repository;
    private final PixApi pixApi;

    public CriarTransacaoUseCase(TransacaoMongoDbRepository repository, PixApi pixApi) {
        this.repository = repository;
        this.pixApi = pixApi;
    }

    public Transacao execute(final CriarTransacaoInput input) {
        validar(input);

        var externalReference = UUID.randomUUID().toString();

        pixApi.criarCobranca(new CriarCobrancaInput(
                input.valor(),
                input.devedorNome(),
                input.devedorCPF(),
                input.descricaoSolicitacao(),
                externalReference
        ));

        var transacao = new Transacao(
                null,
                externalReference,
                AuthUtils.currentResourceOwner(),
                input.valor(),
                input.devedorNome(),
                input.devedorCPF(),
                input.descricaoSolicitacao(),
                null,
                null
        );

        return repository.save(transacao);
    }

    private void validar(CriarTransacaoInput input) {
        if (Objects.isNull(input)) {
            throw new ValidationException("Os dados da transação não podem ser nulos");
        }

        if (Objects.isNull(input.valor())) {
            throw new ValidationException("O valor da transação é obrigatório");
        }

        if (input.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("O valor da transação deve ser maior que zero");
        }

        if (Objects.isNull(input.devedorNome()) || input.devedorNome().isBlank()) {
            throw new ValidationException("O nome do devedor é obrigatório");
        }

        if (Objects.isNull(input.devedorCPF()) || input.devedorCPF().isBlank()) {
            throw new ValidationException("O CPF do devedor é obrigatório");
        }
    }
}
