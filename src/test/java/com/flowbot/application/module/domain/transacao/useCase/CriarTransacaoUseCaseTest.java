package com.flowbot.application.module.domain.transacao.useCase;

import com.flowbot.application.UseCaseTest;
import com.flowbot.application.http.PixApi;
import com.flowbot.application.http.dtos.CriarCobrancaInput;
import com.flowbot.application.module.domain.transacao.StatusTransacao;
import com.flowbot.application.module.domain.transacao.Transacao;
import com.flowbot.application.module.domain.transacao.TransacaoMongoDbRepository;
import com.flowbot.application.module.domain.transacao.api.dto.CriarTransacaoInput;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Criar transacao use case test")
class CriarTransacaoUseCaseTest extends UseCaseTest {

    @InjectMocks
    private CriarTransacaoUseCase useCase;

    @Mock
    private TransacaoMongoDbRepository repository;

    @Mock
    private PixApi pixApi;

    @DisplayName("Deve criar a cobrança PIX e salvar a transação com status SOLICITADO")
    @Test
    void execute() {
        when(repository.save(any(Transacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var input = new CriarTransacaoInput(new BigDecimal("5.00"), "Vanessa Trega", "11936059975", "teste pix");
        var result = useCase.execute(input);

        assertNotNull(result);
        assertEquals(StatusTransacao.SOLICITADO, result.getStatus());
        assertNotNull(result.getExternalReference());
        assertEquals(new BigDecimal("5.00"), result.getValor());

        var captor = ArgumentCaptor.forClass(CriarCobrancaInput.class);
        verify(pixApi).criarCobranca(captor.capture());
        assertEquals(result.getExternalReference(), captor.getValue().externalReference());
        assertEquals("Vanessa Trega", captor.getValue().devedorNome());
    }

    @DisplayName("Não deve permitir valor nulo")
    @Test
    void valorNulo() {
        var input = new CriarTransacaoInput(null, "Vanessa Trega", "11936059975", "teste pix");

        assertThrows(ValidationException.class, () -> useCase.execute(input));
        verifyNoInteractions(pixApi);
        verifyNoInteractions(repository);
    }

    @DisplayName("Não deve permitir valor menor ou igual a zero")
    @Test
    void valorInvalido() {
        var input = new CriarTransacaoInput(BigDecimal.ZERO, "Vanessa Trega", "11936059975", "teste pix");

        assertThrows(ValidationException.class, () -> useCase.execute(input));
        verifyNoInteractions(pixApi);
        verifyNoInteractions(repository);
    }

    @DisplayName("Não deve permitir CPF do devedor em branco")
    @Test
    void cpfEmBranco() {
        var input = new CriarTransacaoInput(new BigDecimal("5.00"), "Vanessa Trega", " ", "teste pix");

        assertThrows(ValidationException.class, () -> useCase.execute(input));
        verifyNoInteractions(pixApi);
        verifyNoInteractions(repository);
    }
}
