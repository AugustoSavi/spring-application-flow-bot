package com.flowbot.application.module.domain.financeiro.assinaturas.api;

import com.flowbot.application.E2ETests;
import com.flowbot.application.context.MultiTenantMongoDatabaseFactory;
import com.flowbot.application.context.TenantThreads;
import com.flowbot.application.module.domain.financeiro.assinaturas.PeriodoPlano;
import com.flowbot.application.module.domain.financeiro.assinaturas.Plano;
import com.flowbot.application.module.domain.financeiro.assinaturas.useCase.CriarPlanoUseCase;
import com.flowbot.application.module.domain.financeiro.assinaturas.useCase.GerenciamentoDoPlanoUseCase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static com.flowbot.application.module.domain.financeiro.assinaturas.PlanoFactory.umPlanoMensal;
import static org.junit.jupiter.api.Assertions.*;

class PlanoMultiTenantTest extends E2ETests {

    private static final String TENANT_1 = "tenant1";
    private static final String TENANT_2 = "tenant2";
    private static final String EMAIL_TENANT_1 = "user1@tenant1.com";
    private static final String EMAIL_TENANT_2 = "user2@tenant2.com";
    @Container
    public static MongoDBContainer MONGO_CONTAINER = new MongoDBContainer(DockerImageName.parse(MONGO_VERSION));
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CriarPlanoUseCase criarPlanoUseCase;
    @Autowired
    private GerenciamentoDoPlanoUseCase gerenciamentoDoPlanoUseCase;

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
    }

    @AfterEach
    void tearDown() {
        TenantThreads.clear();
    }

    @Test
    @DisplayName("Deve garantir que um tenant não consegue ver os planos de outro tenant")
    void naoDeveVerPlanosDeOutroTenant() {
        TenantThreads.setTenantId(TENANT_1);
        var plano1 = umPlanoMensal(EMAIL_TENANT_1);
        mongoTemplate.save(plano1);

        TenantThreads.setTenantId(TENANT_2);
        var plano2 = umPlanoMensal(EMAIL_TENANT_2);
        mongoTemplate.save(plano2);

        TenantThreads.setTenantId(TENANT_1);
        var planosTenant1 = gerenciamentoDoPlanoUseCase.listPlanosAtivos();

        assertEquals(1, planosTenant1.size());
        assertEquals(EMAIL_TENANT_1, planosTenant1.get(0).email());
        assertNotEquals(EMAIL_TENANT_2, planosTenant1.get(0).email());

        TenantThreads.setTenantId(TENANT_2);
        var planosTenant2 = gerenciamentoDoPlanoUseCase.listPlanosAtivos();

        assertEquals(1, planosTenant2.size());
        assertEquals(EMAIL_TENANT_2, planosTenant2.get(0).email());
        assertNotEquals(EMAIL_TENANT_1, planosTenant2.get(0).email());
    }

    @Test
    @DisplayName("Deve garantir que um tenant não consegue obter plano de outro tenant por email")
    void naoDeveObterPlanoDeOutroTenantPorEmail() {
        TenantThreads.setTenantId(TENANT_1);
        var plano1 = umPlanoMensal(EMAIL_TENANT_1);
        mongoTemplate.save(plano1);

        TenantThreads.setTenantId(TENANT_2);
        var plano2 = umPlanoMensal(EMAIL_TENANT_2);
        mongoTemplate.save(plano2);

        TenantThreads.setTenantId(TENANT_1);
        var planoObtido = gerenciamentoDoPlanoUseCase.obterDadosPlano(EMAIL_TENANT_1);
        assertEquals(EMAIL_TENANT_1, planoObtido.email());

        TenantThreads.setTenantId(TENANT_2);
        assertThrows(Exception.class, () -> {
            gerenciamentoDoPlanoUseCase.obterDadosPlano(EMAIL_TENANT_1);
        });
    }

    @Test
    @DisplayName("Deve garantir que cada tenant cria planos em seu próprio banco de dados")
    void deveCriarPlanosEmBancosSeparados() {
        TenantThreads.setTenantId(TENANT_1);
        criarPlanoUseCase.criarPlanoSimples(EMAIL_TENANT_1, PeriodoPlano.MENSAL, true);

        TenantThreads.setTenantId(TENANT_2);
        criarPlanoUseCase.criarPlanoSimples(EMAIL_TENANT_2, PeriodoPlano.ANUAL, true);

        TenantThreads.setTenantId(TENANT_1);
        var planosTenant1 = mongoTemplate.findAll(Plano.class);
        assertEquals(1, planosTenant1.size());
        assertEquals(EMAIL_TENANT_1, planosTenant1.get(0).getUsuario().email());

        TenantThreads.setTenantId(TENANT_2);
        var planosTenant2 = mongoTemplate.findAll(Plano.class);
        assertEquals(1, planosTenant2.size());
        assertEquals(EMAIL_TENANT_2, planosTenant2.get(0).getUsuario().email());
    }

    @Test
    @DisplayName("Deve garantir que tenant sem dados retorna lista vazia")
    void deveRetornarListaVaziaQuandoTenantNaoTemDados() {
        TenantThreads.setTenantId(TENANT_1);
        var plano1 = umPlanoMensal(EMAIL_TENANT_1);
        mongoTemplate.save(plano1);

        var dbNameTenant1 = mongoTemplate.getDb().getName();
        assertEquals(MultiTenantMongoDatabaseFactory.DEFAULT_DATABASE_NAME + "-" + TENANT_1, dbNameTenant1);

        TenantThreads.setTenantId(TENANT_2);
        var dbNameTenant2 = mongoTemplate.getDb().getName();
        assertEquals(MultiTenantMongoDatabaseFactory.DEFAULT_DATABASE_NAME + "-" + TENANT_2, dbNameTenant2);

        var planosTenant2 = gerenciamentoDoPlanoUseCase.listPlanosAtivos();
        assertTrue(planosTenant2.isEmpty(), "Tenant2 não deveria ver planos do Tenant1. Banco: " + dbNameTenant2);
    }
}

