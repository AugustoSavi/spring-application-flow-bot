package com.flowbot.application.module.domain.usuario.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@EnableAsync
public class EnviarEmailConfirmacaoService {

    private static final Logger log = LoggerFactory.getLogger(EnviarEmailConfirmacaoService.class);
    private static final String TEMPLATE_SLUG = "confirmacao-cadastro";

    private final RestClient emailApiRestClient;
    private final String appBaseUrl;

    public EnviarEmailConfirmacaoService(
            @Qualifier("emailApiRestClient") RestClient emailApiRestClient,
            @Value("${app.base-url:http://localhost:8080}") String appBaseUrl) {
        this.emailApiRestClient = emailApiRestClient;
        this.appBaseUrl = appBaseUrl;
    }

    @Async
    public void enviar(String email, String token) {
        try {
            var link = appBaseUrl + "/usuario/confirmar?token=" + token;

            var payload = Map.of(
                    "templateSlug", TEMPLATE_SLUG,
                    "to", email,
                    "subject", "Confirme seu cadastro",
                    "variables", Map.of(
                            "username", email,
                            "link", link
                    )
            );

            emailApiRestClient.post()
                    .uri("/v2/email/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email de confirmação enfileirado para {}", email);
        } catch (Exception e) {
            log.error("Falha ao enfileirar email de confirmação para {}: {}", email, e.getMessage());
        }
    }
}
