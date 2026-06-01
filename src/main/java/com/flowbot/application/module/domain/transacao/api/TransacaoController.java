package com.flowbot.application.module.domain.transacao.api;

import com.flowbot.application.module.domain.transacao.api.dto.ConfirmacaoTransacaoWebhookInput;
import com.flowbot.application.module.domain.transacao.api.dto.CriarTransacaoInput;
import com.flowbot.application.module.domain.transacao.api.dto.TransacaoDtoUtils;
import com.flowbot.application.module.domain.transacao.api.dto.TransacaoOutput;
import com.flowbot.application.module.domain.transacao.useCase.BuscaTransacoesUseCase;
import com.flowbot.application.module.domain.transacao.useCase.ConfirmarPagamentoTransacaoUseCase;
import com.flowbot.application.module.domain.transacao.useCase.CriarTransacaoUseCase;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final CriarTransacaoUseCase criarTransacaoUseCase;
    private final BuscaTransacoesUseCase buscaTransacoesUseCase;
    private final ConfirmarPagamentoTransacaoUseCase confirmarPagamentoTransacaoUseCase;

    public TransacaoController(CriarTransacaoUseCase criarTransacaoUseCase,
                              BuscaTransacoesUseCase buscaTransacoesUseCase,
                              ConfirmarPagamentoTransacaoUseCase confirmarPagamentoTransacaoUseCase) {
        this.criarTransacaoUseCase = criarTransacaoUseCase;
        this.buscaTransacoesUseCase = buscaTransacoesUseCase;
        this.confirmarPagamentoTransacaoUseCase = confirmarPagamentoTransacaoUseCase;
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public TransacaoOutput criar(@RequestBody final CriarTransacaoInput input) {
        return TransacaoDtoUtils.toOutput(criarTransacaoUseCase.execute(input));
    }

    @GetMapping
    public Page<TransacaoOutput> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return TransacaoDtoUtils.toDto(buscaTransacoesUseCase.buscaPaginada(page, size));
    }

    @GetMapping("/{id}")
    public TransacaoOutput buscarPorId(@PathVariable String id) {
        return TransacaoDtoUtils.toOutput(buscaTransacoesUseCase.buscaPorId(id));
    }

    @PostMapping("/webhook/confirmacao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmarPagamento(@RequestBody final ConfirmacaoTransacaoWebhookInput input) {
        confirmarPagamentoTransacaoUseCase.execute(input);
    }
}
