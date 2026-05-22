package com.flowbot.application.module.domain.financeiro.assinaturas.api;

import com.flowbot.application.E2ETests;
import com.flowbot.application.context.TenantThreads;
import com.flowbot.application.module.domain.financeiro.assinaturas.Plano;
import com.flowbot.application.module.domain.financeiro.assinaturas.PeriodoPlano;
import com.flowbot.application.module.domain.financeiro.assinaturas.PlanoFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static com.flowbot.application.module.domain.financeiro.assinaturas.PlanoFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExpirarPlanosVencidosControllerTest extends E2ETests {

    private static final String TENANT_1 = "exp-tenant1";
    private static final String TENANT_2 = "exp-tenant2";
    private static final String EMAIL_VENCIDO_1 = "vencido1@exptest.com";
    private static final String EMAIL_VENCIDO_2 = "vencido2@exptest.com";
    private static final String EMAIL_PAGO_VIGENTE = "pago-vigente@exptest.com";
    private static final String EMAIL_GRATUITO_VENCIDO = "gratuito-vencido@exptest.com";

    @Container
    public static MongoDBContainer MONGO_CONTAINER = new MongoDBContainer(DockerImageName.parse(MONGO_VERSION));

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

    @BeforeEach
    void setUp() {
        TenantThreads.clear();
        TenantThreads.setTenantId(TENANT_1);
        mongoTemplate.dropCollection(Plano.class);
        TenantThreads.setTenantId(TENANT_2);
        mongoTemplate.dropCollection(Plano.class);
        TenantThreads.clear();
    }

    @AfterEach
    void tearDown() {
        TenantThreads.clear();
    }

    @Test
    @DisplayName("Deve marcar como gratuito os planos vencidos em todos os tenants e retornar o total atualizado")
    void deveExpirarPlanosVencidosEmTodosTenants() throws Exception {
        // Arrange
        TenantThreads.setTenantId(TENANT_1);
        mongoTemplate.save(umPlanoPagoVencido(EMAIL_VENCIDO_1));

        TenantThreads.setTenantId(TENANT_2);
        mongoTemplate.save(umPlanoPagoVencido(EMAIL_VENCIDO_2));

        TenantThreads.clear();

        // Act
        var response = mvc.perform(post("/plano/expirar-planos-vencidos"))
                .andDo(print());

        // Assert response
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtualizado").value(2))
                .andExpect(jsonPath("$.tenantsAfetados").isArray())
                .andExpect(jsonPath("$.tenantsAfetados.length()").value(2));

        // Verify DB state: both plans are now gratuito
        TenantThreads.setTenantId(TENANT_1);
        var plano1 = mongoTemplate.findOne(new Query(Criteria.where("usuario.email").is(EMAIL_VENCIDO_1)), Plano.class);
        assertNotNull(plano1);
        assertTrue(plano1.getGratuito());

        TenantThreads.setTenantId(TENANT_2);
        var plano2 = mongoTemplate.findOne(new Query(Criteria.where("usuario.email").is(EMAIL_VENCIDO_2)), Plano.class);
        assertNotNull(plano2);
        assertTrue(plano2.getGratuito());
    }

    @Test
    @DisplayName("Não deve alterar planos pagos ainda vigentes")
    void naoDeveAlterarPlanosVigentes() throws Exception {
        // Arrange: paid plan with finalizaEm in the future
        TenantThreads.setTenantId(TENANT_1);
        mongoTemplate.save(Plano.criarPlano(EMAIL_PAGO_VIGENTE, PeriodoPlano.MENSAL, false));
        TenantThreads.clear();

        // Act
        mvc.perform(post("/plano/expirar-planos-vencidos"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtualizado").value(0));

        // Verify plan remains paid (gratuito=false)
        TenantThreads.setTenantId(TENANT_1);
        var plano = mongoTemplate.findOne(new Query(Criteria.where("usuario.email").is(EMAIL_PAGO_VIGENTE)), Plano.class);
        assertNotNull(plano);
        assertFalse(plano.getGratuito());
    }

    @Test
    @DisplayName("Não deve alterar planos que já são gratuitos mesmo que vencidos")
    void naoDeveAlterarPlanosJaGratuitos() throws Exception {
        // Arrange: gratuito=true expired plan should be skipped
        TenantThreads.setTenantId(TENANT_1);
        mongoTemplate.save(umPlanoGratuitoVencido(EMAIL_GRATUITO_VENCIDO));
        TenantThreads.clear();

        // Act
        mvc.perform(post("/plano/expirar-planos-vencidos"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtualizado").value(0))
                .andExpect(jsonPath("$.tenantsAfetados.length()").value(0));
    }

    @Test
    @DisplayName("Deve retornar zero atualizações quando não há planos vencidos")
    void deveRetornarZeroQuandoNaoHaPlanosVencidos() throws Exception {
        mvc.perform(post("/plano/expirar-planos-vencidos"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtualizado").value(0))
                .andExpect(jsonPath("$.tenantsAfetados").isArray())
                .andExpect(jsonPath("$.tenantsAfetados.length()").value(0));
    }

    @Test
    @DisplayName("Deve processar apenas planos vencidos quando há mix de vigentes e vencidos no mesmo tenant")
    void deveProcessarApenasVencidosNoMesmoTenant() throws Exception {
        // Arrange
        TenantThreads.setTenantId(TENANT_1);
        mongoTemplate.save(umPlanoPagoVencido(EMAIL_VENCIDO_1));
        mongoTemplate.save(Plano.criarPlano(EMAIL_PAGO_VIGENTE, PeriodoPlano.MENSAL, false));
        TenantThreads.clear();

        // Act
        mvc.perform(post("/plano/expirar-planos-vencidos"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtualizado").value(1))
                .andExpect(jsonPath("$.tenantsAfetados.length()").value(1));

        // Verify only the expired plan changed
        TenantThreads.setTenantId(TENANT_1);
        var vencido = mongoTemplate.findOne(new Query(Criteria.where("usuario.email").is(EMAIL_VENCIDO_1)), Plano.class);
        assertTrue(vencido.getGratuito());

        var vigente = mongoTemplate.findOne(new Query(Criteria.where("usuario.email").is(EMAIL_PAGO_VIGENTE)), Plano.class);
        assertFalse(vigente.getGratuito());
    }
}
