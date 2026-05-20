package com.flowbot.application.module.domain.usuario.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class EmailTemplateInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateInitializer.class);
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

    public EmailTemplateInitializer(@Qualifier("emailApiRestClient") RestClient emailApiRestClient) {
        this.emailApiRestClient = emailApiRestClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (templateJaExiste()) {
            log.info("Template de email '{}' já registrado, nenhuma ação necessária.", TEMPLATE_SLUG);
            return;
        }

        registrarTemplate();
    }

    private boolean templateJaExiste() {
        try {
            emailApiRestClient.get()
                    .uri("/v2/email/templates/{slug}", TEMPLATE_SLUG)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.warn("Não foi possível verificar existência do template '{}': {}", TEMPLATE_SLUG, e.getMessage());
            return false;
        }
    }

    private void registrarTemplate() {
        try {
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
        } catch (Exception e) {
            log.error("Falha ao registrar template de email '{}': {}", TEMPLATE_SLUG, e.getMessage());
        }
    }
}
