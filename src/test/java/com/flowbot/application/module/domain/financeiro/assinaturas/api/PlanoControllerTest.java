package com.flowbot.application.module.domain.financeiro.assinaturas.api;

import com.flowbot.application.E2ETests;
import com.flowbot.application.module.domain.financeiro.assinaturas.Plano;
import com.flowbot.application.module.domain.financeiro.assinaturas.api.dto.CriarPlanoInputDto;
import com.flowbot.application.module.domain.financeiro.assinaturas.api.dto.RegistarAcessoDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.flowbot.application.module.domain.financeiro.assinaturas.PlanoFactory.umPlanoMensal;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlanoControllerTest extends E2ETests {

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

    @Test
    @DisplayName("Deve registar um novo acesso com sucesso")
    void registarAcesso() throws Exception {
        // Arrange
        final var email = "john@doe.io";
        mongoTemplate.save(umPlanoMensal(email));
        var registarAcessoDto = new RegistarAcessoDto("web", "192.168.0.1", email);

        var request = post("/plano/acesso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(registarAcessoDto));

        // Act
        var response = this.mvc.perform(request)
                .andDo(print());

        // Assert
        var formatedDate = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstAccess").value(true))
                .andExpect(jsonPath("$.plano.email").value(email))
                .andExpect(jsonPath("$.plano.vigenteAte").value(formatedDate));
    }


    @DisplayName("Cria um plano mensal e depois sobrescreve esse plano por um anual")
    @Test
    void criarEAtulizarPlano() throws Exception {
        // VALIDACAO MENSAL
        var inputDto = new CriarPlanoInputDto("john@doe.io", "MENSAL", null);
        var request = post("/plano")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(inputDto));

        var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk());

        request = get("/plano/vigente?email=john@doe.io")
                .contentType(MediaType.APPLICATION_JSON);

        response = this.mvc.perform(request)
                .andDo(print());

        var formatedDate = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.vigenteAte").value(formatedDate))
                .andExpect(jsonPath("$.email").value("john@doe.io"));

        // FIM DA VALIDACAO MENSAL
        // VALIDACAO ANUAL

        inputDto = new CriarPlanoInputDto("john@doe.io", "ANUAL", null);
        request = post("/plano")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(inputDto));

        response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk());

        request = get("/plano/vigente?email=john@doe.io")
                .contentType(MediaType.APPLICATION_JSON);

        response = this.mvc.perform(request)
                .andDo(print());

        formatedDate = LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.vigenteAte").value(formatedDate))
                .andExpect(jsonPath("$.email").value("john@doe.io"));

        // FIM DA VALIDACAO ANUAL
    }

    @Test
    void criarPlano() throws Exception {
        var inputDto = new CriarPlanoInputDto("john@doe.io", "MENSAL", null);
        final var request = post("/plano")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(inputDto));

        final var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk());
        assertNotNull(response.andReturn().getResponse().getHeader("id"));
    }

    @Test
    void obterPlanoVigente() throws Exception {
        mongoTemplate.dropCollection(Plano.class);
        final var email = "john@doe.io";
        mongoTemplate.save(umPlanoMensal(email));

        final var request = get("/plano/vigente?email=" + email)
                .contentType(MediaType.APPLICATION_JSON);

        final var response = this.mvc.perform(request)
                .andDo(print());

        var formatedDate = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.vigenteAte").value(formatedDate))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void listarPlanosAtivos() throws Exception {
        final var email = "john@doe.io";
        mongoTemplate.save(umPlanoMensal(email));
        mongoTemplate.save(umPlanoMensal("carlos@bol.com"));
        final var request = get("/plano")
                .contentType(MediaType.APPLICATION_JSON);

        final var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value(email))
                .andExpect(jsonPath("$[0].vigenteAte").value(LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
    }

    @Test
    @DisplayName("Deve listar assinaturas ativas com usuario e tempo inscrito ordenado por maior tempo")
    void listarAssinaturasAtivas() throws Exception {
        mongoTemplate.dropCollection(Plano.class);
        mongoTemplate.save(umPlanoMensal("joao@email.com"));
        mongoTemplate.save(umPlanoMensal("maria@email.com"));

        final var request = get("/plano/assinaturas-ativas")
                .contentType(MediaType.APPLICATION_JSON);

        final var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].usuario").exists())
                .andExpect(jsonPath("$[0].ativoHa").exists())
                .andExpect(jsonPath("$[1].usuario").exists())
                .andExpect(jsonPath("$[1].ativoHa").exists());
    }

    @Test
    @DisplayName("gratuito=null (não enviado) deve criar plano pago; false permanece falso; true cria gratuito")
    void criarPlanoComFlagGratuito() throws Exception {
        // null → false (clientes existentes não enviam o campo)
        var semCampo = post("/plano")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"pago-null@email.com\",\"periodoPlano\":\"MENSAL\"}");
        mvc.perform(semCampo).andExpect(status().isOk());

        mvc.perform(get("/plano/vigente?email=pago-null@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gratuito").value(false));

        // gratuito=false → false
        var comFalse = new CriarPlanoInputDto("pago-false@email.com", "MENSAL", false);
        mvc.perform(post("/plano")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(comFalse))).andExpect(status().isOk());

        mvc.perform(get("/plano/vigente?email=pago-false@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gratuito").value(false));

        // gratuito=true → true
        var comTrue = new CriarPlanoInputDto("gratuito-true@email.com", "MENSAL", true);
        mvc.perform(post("/plano")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(comTrue))).andExpect(status().isOk());

        mvc.perform(get("/plano/vigente?email=gratuito-true@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gratuito").value(true));
    }

    @Test
    void solicitarReembolso() throws Exception {
        final var email = "john@doe.io";
        mongoTemplate.save(umPlanoMensal(email));

        final var request = post("/plano/reembolso?email=" + email)
                .contentType(MediaType.APPLICATION_JSON);

        final var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk());

        var requestVigente = get("/plano/vigente?email=" + email)
                .contentType(MediaType.APPLICATION_JSON);

        var responseVigente = this.mvc.perform(requestVigente)
                .andDo(print());

        var formatedDate = LocalDate.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        responseVigente.andExpect(status().isOk())
                .andExpect(jsonPath("$.vigenteAte").value(formatedDate))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.gratuito").value(true));
    }

    @Test
    @DisplayName("Reembolso de plano pago deve alterar gratuito para true")
    void solicitarReembolsoDeveAlterarPlanoParaGratuito() throws Exception {
        final var email = "pago-reembolso@email.com";
        mongoTemplate.save(Plano.criarPlano(email, com.flowbot.application.module.domain.financeiro.assinaturas.PeriodoPlano.MENSAL, false));

        mvc.perform(post("/plano/reembolso?email=" + email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/plano/vigente?email=" + email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gratuito").value(true))
                .andExpect(jsonPath("$.vigenteAte").value(
                        LocalDate.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ));
    }
}
