package com.flowbot.application.module.domain.usuario.useCase;

import com.flowbot.application.UseCaseTest;
import com.flowbot.application.module.domain.financeiro.assinaturas.Plano;
import com.flowbot.application.module.domain.usuario.Usuario;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RegistrarUsuarioUseCaseTest extends UseCaseTest {

    @InjectMocks
    private RegistrarUsuarioUseCase useCase;

    @Mock
    private MongoTemplate adminMongoTemplate;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void deveRegistrarUsuarioComSucesso() {
        when(adminMongoTemplate.exists(any(Query.class), eq(Usuario.class))).thenReturn(false);
        when(mongoTemplate.exists(any(Query.class), eq(Plano.class))).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed-senha");
        when(adminMongoTemplate.save(any(Usuario.class))).thenReturn(new Usuario());
        when(mongoTemplate.save(any(Plano.class))).thenReturn(new Plano());

        useCase.execute("novo@email.com", "senha123");

        verify(adminMongoTemplate, times(1)).save(any(Usuario.class));
        verify(mongoTemplate, times(1)).save(any(Plano.class));
    }

    @Test
    void deveRejeitarEmailJaCadastrado() {
        when(adminMongoTemplate.exists(any(Query.class), eq(Usuario.class))).thenReturn(true);

        assertThrows(ValidationException.class, () -> useCase.execute("existente@email.com", "senha123"));

        verify(adminMongoTemplate, never()).save(any());
        verify(mongoTemplate, never()).save(any());
    }

    @Test
    void deveRejeitarUsuarioComPlanoAtivo() {
        when(adminMongoTemplate.exists(any(Query.class), eq(Usuario.class))).thenReturn(false);
        when(mongoTemplate.exists(any(Query.class), eq(Plano.class))).thenReturn(true);

        assertThrows(ValidationException.class, () -> useCase.execute("comPlano@email.com", "senha123"));

        verify(adminMongoTemplate, never()).save(any());
        verify(mongoTemplate, never()).save(any());
    }
}
