package com.flowbot.application.module.domain.usuario.useCase;

import com.flowbot.application.UseCaseTest;
import com.flowbot.application.module.domain.usuario.TokenConfirmacao;
import com.flowbot.application.module.domain.usuario.Usuario;
import com.flowbot.application.module.domain.usuario.service.EnviarEmailConfirmacaoService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReenviarEmailConfirmacaoUseCaseTest extends UseCaseTest {

    private ReenviarEmailConfirmacaoUseCase useCase;

    @Mock
    private MongoTemplate adminMongoTemplate;

    @Mock
    private EnviarEmailConfirmacaoService enviarEmailConfirmacaoService;

    @BeforeEach
    void setUp() {
        useCase = new ReenviarEmailConfirmacaoUseCase(adminMongoTemplate, enviarEmailConfirmacaoService);
    }

    @Test
    void deveReenviarEmailComSucesso() {
        var usuario = Usuario.criar("usuario@email.com", "hash");
        when(adminMongoTemplate.findOne(any(Query.class), eq(Usuario.class))).thenReturn(usuario);

        useCase.execute("usuario@email.com");

        verify(adminMongoTemplate).remove(any(Query.class), eq(TokenConfirmacao.class));
        verify(adminMongoTemplate).save(any(TokenConfirmacao.class));
        verify(enviarEmailConfirmacaoService).enviar(eq("usuario@email.com"), anyString());
    }

    @Test
    void deveRejeitarUsuarioNaoEncontrado() {
        when(adminMongoTemplate.findOne(any(Query.class), eq(Usuario.class))).thenReturn(null);

        assertThrows(ValidationException.class, () -> useCase.execute("inexistente@email.com"));

        verify(adminMongoTemplate, never()).remove(any(Query.class), eq(TokenConfirmacao.class));
        verify(adminMongoTemplate, never()).save(any());
        verify(enviarEmailConfirmacaoService, never()).enviar(anyString(), anyString());
    }

    @Test
    void deveRejeitarEmailJaConfirmado() {
        var usuario = Usuario.criar("confirmado@email.com", "hash");
        usuario.confirmarEmail();
        when(adminMongoTemplate.findOne(any(Query.class), eq(Usuario.class))).thenReturn(usuario);

        assertThrows(ValidationException.class, () -> useCase.execute("confirmado@email.com"));

        verify(adminMongoTemplate, never()).remove(any(Query.class), eq(TokenConfirmacao.class));
        verify(adminMongoTemplate, never()).save(any());
        verify(enviarEmailConfirmacaoService, never()).enviar(anyString(), anyString());
    }
}
