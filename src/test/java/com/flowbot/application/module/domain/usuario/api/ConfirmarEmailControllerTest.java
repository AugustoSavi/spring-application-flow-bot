package com.flowbot.application.module.domain.usuario.api;

import com.flowbot.application.E2ETests;
import com.flowbot.application.module.domain.usuario.TokenConfirmacao;
import com.flowbot.application.module.domain.usuario.Usuario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConfirmarEmailControllerTest extends E2ETests {

    @Container
    public static MongoDBContainer MONGO_CONTAINER = new MongoDBContainer(DockerImageName.parse(MONGO_VERSION));

    @Autowired
    @Qualifier("adminMongoTemplate")
    private MongoTemplate adminMongoTemplate;

    @DynamicPropertySource
    public static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("mongodb.principal.uri", MONGO_CONTAINER::getReplicaSetUrl);
        registry.add("mongodb.admin.uri", MONGO_CONTAINER::getReplicaSetUrl);
    }

    @BeforeAll
    public static void mongoIsUp() {
        assertTrue(MONGO_CONTAINER.isRunning());
    }

    @Test
    @DisplayName("Deve confirmar email com token válido e marcar emailValidado como true")
    void deveConfirmarEmailComTokenValido() throws Exception {
        final var email = "confirmar@email.com";
        adminMongoTemplate.save(Usuario.criar(email, "hash"));
        var token = TokenConfirmacao.criar(email);
        adminMongoTemplate.save(token);

        mvc.perform(get("/usuario/confirmar?token=" + token.getToken()))
                .andDo(print())
                .andExpect(status().isNoContent());

        var query = new Query().addCriteria(Criteria.where("email").is(email));
        var usuario = adminMongoTemplate.findOne(query, Usuario.class);
        assertNotNull(usuario);
        assertTrue(usuario.getEmailValidado());

        var tokenRemovido = adminMongoTemplate.findById(token.getToken(), TokenConfirmacao.class);
        assertNull(tokenRemovido);
    }

    @Test
    @DisplayName("Deve retornar 400 para token inexistente")
    void deveRetornar400ParaTokenInexistente() throws Exception {
        mvc.perform(get("/usuario/confirmar?token=token-invalido"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 para token expirado")
    void deveRetornar400ParaTokenExpirado() throws Exception {
        final var email = "expirado@email.com";
        adminMongoTemplate.save(Usuario.criar(email, "hash"));
        var tokenExpirado = TokenConfirmacaoTestHelper.criarExpirado(email);
        adminMongoTemplate.save(tokenExpirado);

        mvc.perform(get("/usuario/confirmar?token=" + tokenExpirado.getToken()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
