package com.flowbot.application.module.domain.usuario.useCase;

import com.flowbot.application.module.domain.usuario.TokenConfirmacao;
import com.flowbot.application.module.domain.usuario.Usuario;
import com.flowbot.application.module.domain.usuario.service.EnviarEmailConfirmacaoService;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ReenviarEmailConfirmacaoUseCase {

    private final MongoTemplate adminMongoTemplate;
    private final EnviarEmailConfirmacaoService enviarEmailConfirmacaoService;

    public ReenviarEmailConfirmacaoUseCase(
            @Qualifier("adminMongoTemplate") MongoTemplate adminMongoTemplate,
            EnviarEmailConfirmacaoService enviarEmailConfirmacaoService) {
        this.adminMongoTemplate = adminMongoTemplate;
        this.enviarEmailConfirmacaoService = enviarEmailConfirmacaoService;
    }

    public void execute(String email) {
        var query = new Query().addCriteria(Criteria.where("email").is(email));
        var usuario = adminMongoTemplate.findOne(query, Usuario.class);

        if (usuario == null) {
            throw new ValidationException("Usuário não encontrado");
        }
        if (Boolean.TRUE.equals(usuario.getEmailValidado())) {
            throw new ValidationException("E-mail já confirmado");
        }

        adminMongoTemplate.remove(new Query().addCriteria(Criteria.where("email").is(email)), TokenConfirmacao.class);

        var token = TokenConfirmacao.criar(email);
        adminMongoTemplate.save(token);
        enviarEmailConfirmacaoService.enviar(email, token.getToken());
    }
}
