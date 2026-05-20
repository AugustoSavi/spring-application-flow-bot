package com.flowbot.application.module.domain.financeiro.assinaturas.api;

import com.flowbot.application.context.TenantThreads;
import com.flowbot.application.module.domain.financeiro.assinaturas.PeriodoPlano;
import com.flowbot.application.module.domain.financeiro.assinaturas.PlanoAtivoOutput;
import com.flowbot.application.module.domain.financeiro.assinaturas.api.dto.AcessoOutputDto;
import com.flowbot.application.module.domain.financeiro.assinaturas.api.dto.CriarPlanoInputDto;
import com.flowbot.application.module.domain.financeiro.assinaturas.api.dto.AssinaturaAtivaDto;
import com.flowbot.application.module.domain.financeiro.assinaturas.api.dto.PlanoMultiTenantOutputDto;
import com.flowbot.application.module.domain.financeiro.assinaturas.api.dto.RegistarAcessoDto;
import com.flowbot.application.module.domain.financeiro.assinaturas.useCase.BuscarAssinaturaTodosTenantsUseCase;
import com.flowbot.application.module.domain.financeiro.assinaturas.useCase.CriarPlanoUseCase;
import com.flowbot.application.module.domain.financeiro.assinaturas.useCase.GerenciamentoDoPlanoUseCase;
import com.flowbot.application.module.domain.financeiro.assinaturas.useCase.ListarAssinaturasAtivasUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plano")
public class PlanoController {

    private final CriarPlanoUseCase criarPlanoUseCase;
    private final GerenciamentoDoPlanoUseCase gerenciamentoDoPlanoUseCase;
    private final BuscarAssinaturaTodosTenantsUseCase buscarAssinaturaTodosTenantsUseCase;
    private final ListarAssinaturasAtivasUseCase listarAssinaturasAtivasUseCase;

    public PlanoController(CriarPlanoUseCase criarPlanoUseCase,
                           GerenciamentoDoPlanoUseCase gerenciamentoDoPlanoUseCase,
                           BuscarAssinaturaTodosTenantsUseCase buscarAssinaturaTodosTenantsUseCase,
                           ListarAssinaturasAtivasUseCase listarAssinaturasAtivasUseCase) {
        this.criarPlanoUseCase = criarPlanoUseCase;
        this.gerenciamentoDoPlanoUseCase = gerenciamentoDoPlanoUseCase;
        this.buscarAssinaturaTodosTenantsUseCase = buscarAssinaturaTodosTenantsUseCase;
        this.listarAssinaturasAtivasUseCase = listarAssinaturasAtivasUseCase;
    }

    @GetMapping("/vigente")
    public PlanoAtivoOutput obterPlanoVigente(@RequestParam String email, @RequestParam(required = false) String tenant) {
        var tenantAnterior = TenantThreads.getTenantId();
        try {
            if (tenant != null && !tenant.isEmpty()) {
                TenantThreads.setTenantId(tenant);
            }
            return gerenciamentoDoPlanoUseCase.obterDadosPlano(email);
        } finally {
            if (tenantAnterior != null && !tenantAnterior.isEmpty()) {
                TenantThreads.setTenantId(tenantAnterior);
            } else {
                TenantThreads.clear();
            }
        }
    }

    @PostMapping
    public ResponseEntity<Void> criarPlano(@RequestBody CriarPlanoInputDto dto) {
        PeriodoPlano periodoPlano = PeriodoPlano.valueOf(dto.periodoPlano());
        boolean gratuito = dto.gratuito() != null && dto.gratuito();
        var id = criarPlanoUseCase.criarPlanoSimples(dto.email(), periodoPlano, gratuito);
        HttpHeaders headers = new HttpHeaders();
        headers.add("id", id);
        return ResponseEntity.ok().headers(headers).build();
    }

    @GetMapping
    public List<PlanoAtivoOutput> get() {
        return gerenciamentoDoPlanoUseCase.listPlanosAtivos();
    }

    @PostMapping("/acesso")
    public AcessoOutputDto registarAcesso(@RequestBody RegistarAcessoDto body, @RequestParam(required = false) String tenant) {
        var tenantAnterior = TenantThreads.getTenantId();
        try {
            if (tenant != null && !tenant.isEmpty()) {
                TenantThreads.setTenantId(tenant);
            }
            return gerenciamentoDoPlanoUseCase.registarAcesso(body);
        } finally {
            if (tenantAnterior != null && !tenantAnterior.isEmpty()) {
                TenantThreads.setTenantId(tenantAnterior);
            } else {
                TenantThreads.clear();
            }
        }
    }

    @PostMapping("/reembolso")
    public ResponseEntity<Void> solicitarReembolso(@RequestParam String email) {
        gerenciamentoDoPlanoUseCase.processarReembolso(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/buscar-todos-tenants")
    public List<PlanoMultiTenantOutputDto> buscarAssinaturaTodosTenants(@RequestParam String email) {
        return buscarAssinaturaTodosTenantsUseCase.buscarAssinaturaPorEmail(email);
    }

    @GetMapping("/assinaturas-ativas")
    public List<AssinaturaAtivaDto> listarAssinaturasAtivas() {
        return listarAssinaturasAtivasUseCase.listar();
    }
}
