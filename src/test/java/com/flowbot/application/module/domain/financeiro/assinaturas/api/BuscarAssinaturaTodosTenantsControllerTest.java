package com.flowbot.application.module.domain.financeiro.assinaturas.api;

import com.flowbot.application.E2ETests;
import com.flowbot.application.context.TenantThreads;
import com.flowbot.application.module.domain.financeiro.assinaturas.PeriodoPlano;
import com.flowbot.application.module.domain.financeiro.assinaturas.Plano;
import com.flowbot.application.module.domain.financeiro.assinaturas.useCase.CriarPlanoUseCase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BuscarAssinaturaTodosTenantsControllerTest extends E2ETests {

    private static final String TENANT_1 = "tenant1";
    private static final String TENANT_2 = "tenant2";
    private static final String TENANT_3 = "tenant3";
    private static final String EMAIL_TENANT_1 = "user1@tenant1.com";
    private static final String EMAIL_TENANT_2 = "user2@tenant2.com";
    private static final String EMAIL_TENANT_3 = "user3@tenant3.com";
    private static final String EMAIL_NAO_EXISTE = "naoexiste@example.com";

    @Container
    public static MongoDBContainer MONGO_CONTAINER = new MongoDBContainer(DockerImageName.parse(MONGO_VERSION));

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CriarPlanoUseCase criarPlanoUseCase;

    @DynamicPropertySource
    public static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("mongodb.principal.uri", MONGO_CONTAINER::getReplicaSetUrl);
        registry.add("mongodb.admin.uri", MONGO_CONTAINER::getReplicaSetUrl);
    }

    @BeforeAll
    public static void mongoIsUp() {
        assertTrue(MONGO_CONTAINER.isRunning());
    }

    @BeforeEach
    void setUp() {
        TenantThreads.clear();

        TenantThreads.setTenantId(TENANT_1);
        mongoTemplate.dropCollection(Plano.class);

        TenantThreads.setTenantId(TENANT_2);
        mongoTemplate.dropCollection(Plano.class);

        TenantThreads.setTenantId(TENANT_3);
        mongoTemplate.dropCollection(Plano.class);
    }

    @AfterEach
    void tearDown() {
        TenantThreads.clear();
    }

    @Test
    @DisplayName("Deve buscar assinatura em todos os tenants e retornar múltiplos resultados")
    void deveBuscarAssinaturaEmTodosTenants() throws Exception {
        TenantThreads.setTenantId(TENANT_1);
        criarPlanoUseCase.criarPlanoSimples(EMAIL_TENANT_1, PeriodoPlano.MENSAL, true);

        TenantThreads.setTenantId(TENANT_2);
        criarPlanoUseCase.criarPlanoSimples(EMAIL_TENANT_2, PeriodoPlano.ANUAL, true);

        TenantThreads.setTenantId(TENANT_3);
        criarPlanoUseCase.criarPlanoSimples(EMAIL_TENANT_3, PeriodoPlano.MENSAL, true);

        TenantThreads.clear();

        var request = get("/plano/buscar-todos-tenants")
                .param("email", EMAIL_TENANT_1);

        var response = this.mvc.perform(request)
                .andDo(print());

        var formatedDate = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tenant").value(TENANT_1))
                .andExpect(jsonPath("$[0].plano.email").value(EMAIL_TENANT_1))
                .andExpect(jsonPath("$[0].plano.vigenteAte").value(formatedDate));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando email não existe em nenhum tenant")
    void deveRetornarListaVaziaQuandoEmailNaoExiste() throws Exception {
        TenantThreads.setTenantId(TENANT_1);
        criarPlanoUseCase.criarPlanoSimples(EMAIL_TENANT_1, PeriodoPlano.MENSAL, true);

        TenantThreads.clear();

        var request = get("/plano/buscar-todos-tenants")
                .param("email", EMAIL_NAO_EXISTE);

        var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Deve buscar assinatura apenas em tenants que possuem a collection plano")
    void deveBuscarApenasEmTenantsComCollection() throws Exception {
        TenantThreads.setTenantId(TENANT_1);
        criarPlanoUseCase.criarPlanoSimples(EMAIL_TENANT_1, PeriodoPlano.MENSAL, true);

        TenantThreads.setTenantId(TENANT_2);
        mongoTemplate.dropCollection(Plano.class);

        TenantThreads.clear();

        var request = get("/plano/buscar-todos-tenants")
                .param("email", EMAIL_TENANT_1);

        var response = this.mvc.perform(request)
                .andDo(print());

        var formatedDate = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tenant").value(TENANT_1))
                .andExpect(jsonPath("$[0].plano.email").value(EMAIL_TENANT_1))
                .andExpect(jsonPath("$[0].plano.vigenteAte").value(formatedDate));
    }

    @Test
    @DisplayName("Deve buscar assinatura em múltiplos tenants quando o mesmo email existe em diferentes tenants")
    void deveBuscarAssinaturaEmMultiplosTenantsComMesmoEmail() throws Exception {
        var emailComum = "comum@example.com";

        TenantThreads.setTenantId(TENANT_1);
        criarPlanoUseCase.criarPlanoSimples(emailComum, PeriodoPlano.MENSAL, true);

        TenantThreads.setTenantId(TENANT_2);
        criarPlanoUseCase.criarPlanoSimples(emailComum, PeriodoPlano.ANUAL, true);

        TenantThreads.clear();

        var request = get("/plano/buscar-todos-tenants")
                .param("email", emailComum);

        var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].plano.email").value(emailComum))
                .andExpect(jsonPath("$[1].plano.email").value(emailComum));
    }
}
