package com.flowbot.application.module.domain.usuario.useCase;

import com.flowbot.application.context.TenantThreads;
import com.flowbot.application.module.domain.financeiro.assinaturas.Plano;
import com.flowbot.application.module.domain.financeiro.assinaturas.PeriodoPlano;
import com.flowbot.application.module.domain.usuario.TokenConfirmacao;
import com.flowbot.application.module.domain.usuario.Usuario;
import com.flowbot.application.module.domain.usuario.service.EnviarEmailConfirmacaoService;
import com.flowbot.application.shared.AuthUtils;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrarUsuarioUseCase {

    private final MongoTemplate adminMongoTemplate;
    private final MongoTemplate mongoTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EnviarEmailConfirmacaoService enviarEmailConfirmacaoService;

    public RegistrarUsuarioUseCase(
            @Qualifier("adminMongoTemplate") MongoTemplate adminMongoTemplate,
            MongoTemplate mongoTemplate,
            PasswordEncoder passwordEncoder,
            EnviarEmailConfirmacaoService enviarEmailConfirmacaoService) {
        this.adminMongoTemplate = adminMongoTemplate;
        this.mongoTemplate = mongoTemplate;
        this.passwordEncoder = passwordEncoder;
        this.enviarEmailConfirmacaoService = enviarEmailConfirmacaoService;
    }

    public void execute(String email, String senha) {
        validarEmailNaoRegistrado(email);
        AuthUtils.setTenantFromEmail(email);
        try {
            validarSemPlanoAtivo(email);
            var senhaHash = passwordEncoder.encode(senha);
            adminMongoTemplate.save(Usuario.criar(email, senhaHash));
            mongoTemplate.save(Plano.criarPlano(email, PeriodoPlano.MENSAL, true));
            var token = TokenConfirmacao.criar(email);
            adminMongoTemplate.save(token);
            enviarEmailConfirmacaoService.enviar(email, token.getToken());
        } finally {
            TenantThreads.clear();
        }
    }

    private void validarEmailNaoRegistrado(String email) {
        var query = new Query().addCriteria(Criteria.where("email").is(email));
        if (adminMongoTemplate.exists(query, Usuario.class)) {
            throw new ValidationException("Email já cadastrado");
        }
    }

    private void validarSemPlanoAtivo(String email) {
        var query = new Query().addCriteria(
                Criteria.where("usuario.email").is(email).and("ativo").is(true)
        );
        if (mongoTemplate.exists(query, Plano.class)) {
            throw new ValidationException("Usuário já possui um plano ativo");
        }
    }
}
