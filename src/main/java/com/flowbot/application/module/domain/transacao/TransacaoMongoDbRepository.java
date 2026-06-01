package com.flowbot.application.module.domain.transacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TransacaoMongoDbRepository extends MongoRepository<Transacao, String> {

    Page<Transacao> findAllByResourceOwner(String resourceOwner, Pageable pageable);

    Optional<Transacao> findByIdAndResourceOwner(String id, String resourceOwner);
}
