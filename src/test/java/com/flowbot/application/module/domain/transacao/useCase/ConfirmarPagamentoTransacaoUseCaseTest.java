package com.flowbot.application.module.domain.transacao.useCase;

import com.flowbot.application.UseCaseTest;
import com.flowbot.application.module.domain.transacao.StatusTransacao;
import com.flowbot.application.module.domain.transacao.Transacao;
import com.flowbot.application.module.domain.transacao.TransacaoMongoDbRepository;
import com.flowbot.application.module.domain.transacao.api.dto.ConfirmacaoTransacaoWebhookInput;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Confirmar pagamento transacao use case test")
class ConfirmarPagamentoTransacaoUseCaseTest extends UseCaseTest {

    @InjectMocks
    private ConfirmarPagamentoTransacaoUseCase useCase;

    @Mock
    private TransacaoMongoDbRepository repository;

    @DisplayName("Deve atualizar o status da transação para PAGAMENTO_EFETUADO")
    @Test
    void confirmarPagamento() {
        var transacao = new Transacao("id-1", "ext-ref-123", "client-abc",
                new BigDecimal("100.00"), "João Silva", "12345678900", "Pagamento teste",
                StatusTransacao.SOLICITADO, null);

        when(repository.findByExternalReferenceAndResourceOwner("ext-ref-123", "client-abc"))
                .thenReturn(Optional.of(transacao));
        when(repository.save(any(Transacao.class))).thenAnswer(i -> i.getArgument(0));

        var input = new ConfirmacaoTransacaoWebhookInput("client-abc", "ext-ref-123", "https://cliente.com/webhook");
        useCase.execute(input);

        var captor = ArgumentCaptor.forClass(Transacao.class);
        verify(repository).save(captor.capture());
        assertEquals(StatusTransacao.PAGAMENTO_EFETUADO, captor.getValue().getStatus());
    }

    @DisplayName("Deve lançar exceção quando a transação não for encontrada")
    @Test
    void transacaoNaoEncontrada() {
        when(repository.findByExternalReferenceAndResourceOwner(any(), any())).thenReturn(Optional.empty());

        var input = new ConfirmacaoTransacaoWebhookInput("client-abc", "ext-ref-inexistente", null);

        assertThrows(ValidationException.class, () -> useCase.execute(input));
        verify(repository, never()).save(any());
    }

    @DisplayName("Deve lançar exceção quando clientID for nulo")
    @Test
    void clientIdNulo() {
        var input = new ConfirmacaoTransacaoWebhookInput(null, "ext-ref-123", null);

        assertThrows(ValidationException.class, () -> useCase.execute(input));
        verifyNoInteractions(repository);
    }

    @DisplayName("Deve lançar exceção quando externalRef for vazio")
    @Test
    void externalRefVazio() {
        var input = new ConfirmacaoTransacaoWebhookInput("client-abc", " ", null);

        assertThrows(ValidationException.class, () -> useCase.execute(input));
        verifyNoInteractions(repository);
    }

    @DisplayName("Deve lançar exceção quando payload for nulo")
    @Test
    void payloadNulo() {
        assertThrows(ValidationException.class, () -> useCase.execute(null));
        verifyNoInteractions(repository);
    }
}
