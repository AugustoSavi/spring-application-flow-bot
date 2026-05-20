package com.flowbot.application.module.domain.usuario.useCase;

import com.flowbot.application.module.domain.usuario.TokenConfirmacao;
import com.flowbot.application.module.domain.usuario.Usuario;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ConfirmarEmailUseCase {

    private final MongoTemplate adminMongoTemplate;

    public ConfirmarEmailUseCase(@Qualifier("adminMongoTemplate") MongoTemplate adminMongoTemplate) {
        this.adminMongoTemplate = adminMongoTemplate;
    }

    public void execute(String token) {
        var tokenConfirmacao = adminMongoTemplate.findById(token, TokenConfirmacao.class);

        if (tokenConfirmacao == null) {
            throw new ValidationException("Token inválido");
        }
        if (tokenConfirmacao.estaExpirado()) {
            throw new ValidationException("Token expirado");
        }

        var query = new Query().addCriteria(Criteria.where("email").is(tokenConfirmacao.getEmail()));
        var usuario = adminMongoTemplate.findOne(query, Usuario.class);

        if (usuario == null) {
            throw new ValidationException("Usuário não encontrado");
        }

        usuario.confirmarEmail();
        adminMongoTemplate.save(usuario);
        adminMongoTemplate.remove(tokenConfirmacao);
    }
}
