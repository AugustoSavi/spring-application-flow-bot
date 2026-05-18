package com.flowbot.application.module.domain.financeiro.assinaturas.useCase;

import com.flowbot.application.module.domain.financeiro.assinaturas.PeriodoPlano;
import com.flowbot.application.module.domain.financeiro.assinaturas.Plano;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class CriarPlanoUseCase {
    private static final Logger log = LoggerFactory.getLogger(CriarPlanoUseCase.class);
    private final MongoTemplate mongoTemplate;

    public CriarPlanoUseCase(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public String criarPlanoSimples(String email, PeriodoPlano periodoPlano, boolean gratuito) {
        removerPlanoCasoExista(email);
        return mongoTemplate.save(Plano.criarPlano(email, periodoPlano, gratuito)).getId();
    }

    private void removerPlanoCasoExista(String email) {
        var query = new Query().addCriteria(Criteria.where("usuario.email").is(email));
        var remove = mongoTemplate.findAndRemove(query, Plano.class);
        if (remove != null) {
            log.info("Plano antigo removido: {}", remove);
        }
    }
}
