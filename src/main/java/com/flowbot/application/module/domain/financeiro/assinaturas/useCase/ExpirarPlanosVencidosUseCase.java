package com.flowbot.application.module.domain.financeiro.assinaturas.useCase;

import com.flowbot.application.context.MultiTenantMongoDatabaseFactory;
import com.flowbot.application.module.domain.financeiro.assinaturas.Plano;
import com.flowbot.application.module.domain.financeiro.assinaturas.api.dto.ExpirarPlanosVencidosResultDto;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class ExpirarPlanosVencidosUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExpirarPlanosVencidosUseCase.class);
    private static final String COLLECTION_NAME = "plano";

    private final String connectionString;

    public ExpirarPlanosVencidosUseCase(@Value("${spring.data.mongodb.uri}") String connectionString) {
        this.connectionString = connectionString;
    }

    public ExpirarPlanosVencidosResultDto expirar() {
        int totalAtualizado = 0;
        List<String> tenantsAfetados = new ArrayList<>();

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            List<String> databaseNames = StreamSupport.stream(
                    mongoClient.listDatabaseNames().spliterator(), false
            ).toList();

            for (String dbName : databaseNames) {
                if (!dbName.startsWith(MultiTenantMongoDatabaseFactory.DEFAULT_DATABASE_NAME + "-")) {
                    continue;
                }

                try {
                    MongoDatabase database = mongoClient.getDatabase(dbName);
                    boolean collectionExists = StreamSupport.stream(
                            database.listCollectionNames().spliterator(), false
                    ).anyMatch(name -> name.equals(COLLECTION_NAME));

                    if (!collectionExists) {
                        continue;
                    }

                    String tenantId = dbName.substring(MultiTenantMongoDatabaseFactory.DEFAULT_DATABASE_NAME.length() + 1);
                    MongoTemplate tenantMongoTemplate = new MongoTemplate(
                            new SimpleMongoClientDatabaseFactory(mongoClient, dbName)
                    );

                    Query query = new Query(
                            Criteria.where("finalizaEm").lt(LocalDateTime.now())
                                    .and("gratuito").is(false)
                    );
                    Update update = new Update().set("gratuito", true);
                    var result = tenantMongoTemplate.updateMulti(query, update, Plano.class);

                    if (result.getModifiedCount() > 0) {
                        totalAtualizado += (int) result.getModifiedCount();
                        tenantsAfetados.add(tenantId);
                        log.info("Tenant {}: {} plano(s) vencido(s) convertido(s) para gratuito", tenantId, result.getModifiedCount());
                    }

                } catch (Exception e) {
                    log.warn("Erro ao processar banco {}: {}", dbName, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Erro ao listar bancos de dados: {}", e.getMessage(), e);
        }

        log.info("Expiração concluída: {} plano(s) atualizado(s) em {} tenant(s)", totalAtualizado, tenantsAfetados.size());
        return new ExpirarPlanosVencidosResultDto(totalAtualizado, tenantsAfetados);
    }
}
