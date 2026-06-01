package com.flowbot.application.http;

import com.flowbot.application.configs.properties.PixApiProperties;
import com.flowbot.application.http.dtos.CriarCobrancaInput;
import com.flowbot.application.http.dtos.CriarCobrancaOutput;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;

import static com.flowbot.application.utils.HttpUtils.is4xx;
import static com.flowbot.application.utils.HttpUtils.is5xx;

@Component
public class DefaultPixApi implements PixApi {

    private static final Logger log = LoggerFactory.getLogger(DefaultPixApi.class);

    private final RestClient pixApiRestClient;
    private final PixApiProperties properties;

    public DefaultPixApi(@Qualifier("pixApiRestClient") RestClient pixApiRestClient,
                         PixApiProperties properties) {
        this.pixApiRestClient = pixApiRestClient;
        this.properties = properties;
    }

    @Override
    public CriarCobrancaOutput criarCobranca(CriarCobrancaInput input) {
        var body = new HashMap<String, Object>();
        body.put("chavePix", properties.getChavePix());
        body.put("valor", input.valor());
        body.put("devedorNome", input.devedorNome());
        body.put("devedorCPF", input.devedorCPF());
        body.put("descricaoSolicitacao", input.descricaoSolicitacao());
        body.put("externalReference", input.externalReference());

        var response = pixApiRestClient.post()
                .uri("/pix/criar-cobranca")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(is4xx, errorHandler())
                .onStatus(is5xx, errorHandler())
                .body(CriarCobrancaOutput.class);

        log.info("Cobrança PIX criada para externalReference {}: {}", input.externalReference(), response);
        return response;
    }

    private RestClient.ResponseSpec.ErrorHandler errorHandler() {
        return (req, res) -> {
            log.error("Falha ao criar cobrança PIX [{}] status: {}", req.getURI(), res.getStatusCode());
            throw new ValidationException("Falha ao criar a cobrança PIX");
        };
    }
}
