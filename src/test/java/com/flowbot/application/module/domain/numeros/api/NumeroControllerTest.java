package com.flowbot.application.module.domain.numeros.api;

import com.flowbot.application.E2ETests;
import com.flowbot.application.module.domain.numeros.NumeroMongoDbRepository;
import com.flowbot.application.module.domain.numeros.api.dto.CriarNovoNumeroDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.List;

import static com.flowbot.application.module.domain.numeros.NumerosFactory.umNumero;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("NumeroController Test")
class NumeroControllerTest extends E2ETests {

    @Autowired
    private NumeroMongoDbRepository repository;

    @Container
    public static MongoDBContainer MONGO_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:6.0.5"));

    @DynamicPropertySource
    public static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_CONTAINER::getReplicaSetUrl);
    }

    @BeforeAll
    public static void mongoIsUp() {
        assertTrue(MONGO_CONTAINER.isRunning());
    }

    @DisplayName("Deve criar um numero com sucesso")
    @Test
    void criarNumero() throws Exception {
        final var input = new CriarNovoNumeroDto("nick", "numero");
        final var request = post("/numeros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(input));

        final var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk());
        // validate if response.headers contains property id
        assertNotNull(response.andReturn().getResponse().getHeader("id"));
    }

    @DisplayName("Deve buscar um registro por Id")
    @Test
    void buscarPorId() throws Exception {
        final var numero = repository.save(umNumero("P"));
        final var request = get("/numeros/" + numero.getId())
                .contentType(MediaType.APPLICATION_JSON);

        final var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", instanceOf(String.class)))
                .andExpect(jsonPath("$.dataCriacao").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.whatsappInternalId").isEmpty())
                .andExpect(jsonPath("$.statusNumero").value("CRIADO"))
                .andExpect(jsonPath("$.nick").value("P"));
    }

    @DisplayName("Deve fazer a busca padrao")
    @Test
    void listar() throws Exception {
        repository.deleteAll();
        repository.saveAll(List.of(
                umNumero("P"),
                umNumero("X"),
                umNumero("A"),
                umNumero("Primeiro")
        ));

        final var request = get("/numeros")
                .contentType(MediaType.APPLICATION_JSON)
                .param("sortByNewest", "true");

        final var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", instanceOf(String.class)))
                .andExpect(jsonPath("$.content[0].nick").value("P"))
                .andExpect(jsonPath("$.content[1].nick").value("X"))
                .andExpect(jsonPath("$.content[2].nick").value("A"))
                .andExpect(jsonPath("$.content[3].nick").value("Primeiro"));
    }

    @DisplayName("Deve fazer a busca com sort DESC")
    @Test
    void listarSort() throws Exception {
        repository.deleteAll();
        repository.save(umNumero("P"));
        repository.save(umNumero("X"));
        repository.save(umNumero("A"));
        repository.save(umNumero("Primeiro"));

        final var request = get("/numeros")
                .contentType(MediaType.APPLICATION_JSON)
                .param("sortByNewest", "false");

        final var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", instanceOf(String.class)))
                .andExpect(jsonPath("$.content[3].nick").value("P"))
                .andExpect(jsonPath("$.content[2].nick").value("X"))
                .andExpect(jsonPath("$.content[1].nick").value("A"))
                .andExpect(jsonPath("$.content[0].nick").value("Primeiro"));
    }

    @DisplayName("Deve fazer a busca por filtro de term")
    @Test
    void listarFiltroTerm() throws Exception {
        repository.deleteAll();
        repository.saveAll(List.of(
                umNumero("P"),
                umNumero("X"),
                umNumero("A"),
                umNumero("Primeiro")
        ));

        final var request = get("/numeros")
                .contentType(MediaType.APPLICATION_JSON)
                .param("sortByNewest", "true")
                .param("terms", "p");

        final var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements").value(is(2)))
                .andExpect(jsonPath("$.content[0].nick").value("P"))
                .andExpect(jsonPath("$.content[1].nick").value("Primeiro"));
    }

    @DisplayName("Deve fazer a busca por Status")
    @Test
    void listarFiltroStatus() throws Exception {
        repository.deleteAll();
        repository.saveAll(List.of(
                umNumero("P"),
                umNumero("X"),
                umNumero("A"),
                umNumero("Primeiro")
        ));

        final var request = get("/numeros")
                .contentType(MediaType.APPLICATION_JSON)
                .param("sortByNewest", "true")
                .param("status", "banido");

        final var response = this.mvc.perform(request)
                .andDo(print());

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements").value(is(0)))
                .andExpect(jsonPath("$.content").isEmpty());
    }
}