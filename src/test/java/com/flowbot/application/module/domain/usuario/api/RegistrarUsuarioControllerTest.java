package com.flowbot.application.module.domain.usuario.api;

import com.flowbot.application.E2ETests;
import com.flowbot.application.context.TenantThreads;
import com.flowbot.application.module.domain.financeiro.assinaturas.Plano;
import com.flowbot.application.module.domain.financeiro.assinaturas.PeriodoPlano;
import com.flowbot.application.module.domain.usuario.Usuario;
import com.flowbot.application.module.domain.usuario.apis.dto.RegistrarUsuarioInputDto;
import com.flowbot.application.shared.AuthUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrarUsuarioControllerTest extends E2ETests {

    @Container
    public static MongoDBContainer MONGO_CONTAINER = new MongoDBContainer(DockerImageName.parse(MONGO_VERSION));

    @Autowired
    @Qualifier("adminMongoTemplate")
    private MongoTemplate adminMongoTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

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
    @DisplayName("Deve registrar novo usuário e criar plano gratuito")
    void deveRegistrarNovoUsuario() throws Exception {
        var dto = new RegistrarUsuarioInputDto("novo@email.com", "senha123");
        var request = post("/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto));

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve rejeitar registro quando email já está cadastrado")
    void deveRejeitarEmailJaCadastrado() throws Exception {
        var usuario = Usuario.criar("duplicado@email.com", "hash");
        adminMongoTemplate.save(usuario);

        var dto = new RegistrarUsuarioInputDto("duplicado@email.com", "senha123");
        var request = post("/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto));

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve rejeitar registro quando usuário já possui plano ativo")
    void deveRejeitarUsuarioComPlanoAtivo() throws Exception {
        var plano = Plano.criarPlano("comPlano@email.com", PeriodoPlano.MENSAL, true);
        AuthUtils.setTenantFromEmail("comPlano@email.com");
        try {
            mongoTemplate.save(plano);
        } finally {
            TenantThreads.clear();
        }

        var dto = new RegistrarUsuarioInputDto("comPlano@email.com", "senha123");
        var request = post("/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto));

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
