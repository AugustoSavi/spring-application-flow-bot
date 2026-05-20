package com.flowbot.application.module.domain.usuario.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@EnableAsync
public class EnviarEmailConfirmacaoService {

    private static final Logger log = LoggerFactory.getLogger(EnviarEmailConfirmacaoService.class);
    private static final String TEMPLATE_SLUG = "confirmacao-cadastro";

    private static final String TEMPLATE_HTML = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Confirme seu cadastro</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f4f4f4;font-family:Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f4;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                      <tr>
                        <td style="background-color:#25D366;padding:30px;text-align:center;">
                          <h1 style="color:#ffffff;margin:0;font-size:24px;">Confirme seu cadastro</h1>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:40px 30px;">
                          <p style="color:#333333;font-size:16px;margin:0 0 20px;">Olá, <strong>{{.username}}</strong>!</p>
                          <p style="color:#555555;font-size:15px;margin:0 0 30px;">
                            Obrigado por se cadastrar. Para ativar sua conta, clique no botão abaixo para confirmar seu endereço de e-mail.
                          </p>
                          <table cellpadding="0" cellspacing="0" style="margin:0 auto 30px;">
                            <tr>
                              <td style="background-color:#25D366;border-radius:6px;">
                                <a href="{{.link}}" style="display:inline-block;padding:14px 32px;color:#ffffff;text-decoration:none;font-size:16px;font-weight:bold;">
                                  Confirmar e-mail
                                </a>
                              </td>
                            </tr>
                          </table>
                          <p style="color:#888888;font-size:13px;margin:0;">
                            Se você não criou uma conta, ignore este e-mail.<br/>
                            Este link expira em 24 horas.
                          </p>
                        </td>
                      </tr>
                      <tr>
                        <td style="background-color:#f9f9f9;padding:20px 30px;text-align:center;">
                          <p style="color:#aaaaaa;font-size:12px;margin:0;">
                            &copy; FlowBot &mdash; todos os direitos reservados
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;

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
            garantirTemplateRegistrado();

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

    private void garantirTemplateRegistrado() {
        try {
            emailApiRestClient.get()
                    .uri("/v2/email/templates/{slug}", TEMPLATE_SLUG)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            registrarTemplate();
        }
    }

    private void registrarTemplate() {
        var payload = Map.of(
                "slug", TEMPLATE_SLUG,
                "name", "Confirmação de Cadastro",
                "htmlTemplate", TEMPLATE_HTML
        );

        emailApiRestClient.post()
                .uri("/v2/email/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();

        log.info("Template de email '{}' registrado com sucesso.", TEMPLATE_SLUG);
    }
}
