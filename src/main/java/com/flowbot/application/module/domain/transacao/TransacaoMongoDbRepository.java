package com.flowbot.application.module.domain.transacao;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransacaoMongoDbRepository extends MongoRepository<Transacao, String> {
}
