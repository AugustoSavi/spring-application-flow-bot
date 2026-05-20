package com.flowbot.application.module.domain.usuario.apis;

import com.flowbot.application.context.TenantThreads;
import com.flowbot.application.module.domain.usuario.apis.dto.RegistrarUsuarioInputDto;
import com.flowbot.application.module.domain.usuario.apis.dto.TenantDto;
import com.flowbot.application.module.domain.usuario.useCase.ConfirmarEmailUseCase;
import com.flowbot.application.module.domain.usuario.useCase.ReenviarEmailConfirmacaoUseCase;
import com.flowbot.application.module.domain.usuario.useCase.RegistrarUsuarioUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final ConfirmarEmailUseCase confirmarEmailUseCase;
    private final ReenviarEmailConfirmacaoUseCase reenviarEmailConfirmacaoUseCase;

    public UsuarioController(
            RegistrarUsuarioUseCase registrarUsuarioUseCase,
            ConfirmarEmailUseCase confirmarEmailUseCase,
            ReenviarEmailConfirmacaoUseCase reenviarEmailConfirmacaoUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.confirmarEmailUseCase = confirmarEmailUseCase;
        this.reenviarEmailConfirmacaoUseCase = reenviarEmailConfirmacaoUseCase;
    }

    @GetMapping("/tenant")
    public TenantDto obterTenant() {
        var tenantId = TenantThreads.getTenantId();
        return new TenantDto(tenantId);
    }

    @PostMapping
    public ResponseEntity<Void> registrar(@RequestBody RegistrarUsuarioInputDto dto) {
        registrarUsuarioUseCase.execute(dto.email(), dto.senha());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/confirmar")
    public ResponseEntity<Void> confirmarEmail(@RequestParam String token) {
        confirmarEmailUseCase.execute(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reenviar-confirmacao")
    public ResponseEntity<Void> reenviarConfirmacao(@RequestParam String email) {
        reenviarEmailConfirmacaoUseCase.execute(email);
        return ResponseEntity.noContent().build();
    }
}
