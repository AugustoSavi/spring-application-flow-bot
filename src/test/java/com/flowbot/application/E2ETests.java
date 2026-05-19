package com.flowbot.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowbot.application.http.BotBuilderApi;
import com.flowbot.application.module.domain.usuario.service.EnviarEmailConfirmacaoService;
import com.flowbot.application.shared.ApplicationScheduleService;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@Tag("e2e")
public abstract class E2ETests {

    public static final String MONGO_VERSION = "mongo:8.0";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @MockBean
    protected ApplicationScheduleService applicationScheduleService;

    @MockBean
    protected BotBuilderApi botBuilderApi;

    @MockBean
    protected EnviarEmailConfirmacaoService enviarEmailConfirmacaoService;
}
