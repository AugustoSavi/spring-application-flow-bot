package com.flowbot.application.module.domain.usuario.useCase;

import com.flowbot.application.UseCaseTest;
import com.flowbot.application.context.TenantThreads;
import com.flowbot.application.module.domain.financeiro.assinaturas.Plano;
import com.flowbot.application.module.domain.usuario.TokenConfirmacao;
import com.flowbot.application.module.domain.usuario.Usuario;
import com.flowbot.application.module.domain.usuario.service.EnviarEmailConfirmacaoService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RegistrarUsuarioUseCaseTest extends UseCaseTest {

    private RegistrarUsuarioUseCase useCase;

    @Mock
    private MongoTemplate adminMongoTemplate;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EnviarEmailConfirmacaoService enviarEmailConfirmacaoService;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarUsuarioUseCase(adminMongoTemplate, mongoTemplate, passwordEncoder, enviarEmailConfirmacaoService);
    }

    @AfterEach
    void clearTenant() {
        TenantThreads.clear();
    }

    @Test
    void deveRegistrarUsuarioComSucesso() {
        when(adminMongoTemplate.exists(any(Query.class), eq(Usuario.class))).thenReturn(false);
        when(mongoTemplate.exists(any(Query.class), eq(Plano.class))).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed-senha");

        useCase.execute("novo@email.com", "senha123", "0b0872ee");

        verify(adminMongoTemplate).save(any(Usuario.class));
        verify(mongoTemplate).save(any(Plano.class));
        verify(adminMongoTemplate).save(any(TokenConfirmacao.class));
        verify(enviarEmailConfirmacaoService).enviar(eq("novo@email.com"), anyString());
    }

    @Test
    @DisplayName("Deve usar o tenant fornecido, não calcular pelo email")
    void deveUsarTenantFornecidoNaoCalcularPeloEmail() {
        when(adminMongoTemplate.exists(any(Query.class), eq(Usuario.class))).thenReturn(false);
        when(mongoTemplate.exists(any(Query.class), eq(Plano.class))).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed-senha");

        var tenantCapturado = new String[1];
        doAnswer(invocation -> {
            tenantCapturado[0] = TenantThreads.getTenantId();
            return null;
        }).when(mongoTemplate).save(any(Plano.class));

        useCase.execute("appmeconectei@gmail.com", "senha123", "0b0872ee");

        assertEquals("0b0872ee", tenantCapturado[0]);
    }

    @Test
    void deveRejeitarEmailJaCadastrado() {
        when(adminMongoTemplate.exists(any(Query.class), eq(Usuario.class))).thenReturn(true);

        assertThrows(ValidationException.class, () -> useCase.execute("existente@email.com", "senha123", "0b0872ee"));

        verify(adminMongoTemplate, never()).save(any());
        verify(mongoTemplate, never()).save(any());
    }

    @Test
    void deveRejeitarUsuarioComPlanoAtivo() {
        when(adminMongoTemplate.exists(any(Query.class), eq(Usuario.class))).thenReturn(false);
        when(mongoTemplate.exists(any(Query.class), eq(Plano.class))).thenReturn(true);

        assertThrows(ValidationException.class, () -> useCase.execute("comPlano@email.com", "senha123", "0b0872ee"));

        verify(adminMongoTemplate, never()).save(any());
        verify(mongoTemplate, never()).save(any());
    }
}
