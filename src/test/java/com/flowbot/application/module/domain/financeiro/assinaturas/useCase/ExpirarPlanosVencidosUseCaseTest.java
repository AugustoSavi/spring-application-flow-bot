package com.flowbot.application.module.domain.financeiro.assinaturas.useCase;

import com.flowbot.application.UseCaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpirarPlanosVencidosUseCaseTest extends UseCaseTest {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017/test";
    private ExpirarPlanosVencidosUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ExpirarPlanosVencidosUseCase(CONNECTION_STRING);
    }

    @Test
    @DisplayName("Deve retornar resultado com zero atualizações quando não consegue conectar ao banco")
    void deveRetornarZeroQuandoNaoHaConexao() {
        var result = useCase.expirar();

        assertNotNull(result);
        assertEquals(0, result.totalAtualizado());
        assertNotNull(result.tenantsAfetados());
        assertTrue(result.tenantsAfetados().isEmpty());
    }

    @Test
    @DisplayName("Deve retornar objeto não nulo mesmo com string de conexão inválida")
    void deveRetornarObjetoNaoNuloComConexaoInvalida() {
        var useCaseInvalido = new ExpirarPlanosVencidosUseCase("mongodb://host-invalido:99999/test");

        var result = useCaseInvalido.expirar();

        assertNotNull(result);
        assertNotNull(result.tenantsAfetados());
    }
}
