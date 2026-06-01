package com.flowbot.application.module.domain.transacao.useCase;

import com.flowbot.application.module.domain.transacao.Transacao;
import com.flowbot.application.module.domain.transacao.TransacaoMongoDbRepository;
import com.flowbot.application.shared.AuthUtils;
import jakarta.validation.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BuscaTransacoesUseCase {

    private final TransacaoMongoDbRepository repository;

    public BuscaTransacoesUseCase(TransacaoMongoDbRepository repository) {
        this.repository = repository;
    }

    public Transacao buscaPorId(String id) {
        return repository.findByIdAndResourceOwner(id, AuthUtils.currentResourceOwner())
                .orElseThrow(() -> new ValidationException("Transação não encontrada"));
    }

    public Page<Transacao> buscaPaginada(final int page, final int size) {
        var pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataCriacao"));
        return repository.findAllByResourceOwner(AuthUtils.currentResourceOwner(), pageRequest);
    }
}
