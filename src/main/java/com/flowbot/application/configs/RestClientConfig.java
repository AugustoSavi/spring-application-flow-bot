package com.flowbot.application.configs;

import com.flowbot.application.configs.properties.BotBuilderEngineApiProperties;
import com.flowbot.application.configs.properties.EmailApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Configuration
public class RestClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RestClientConfig.class);

    @Bean
    public RestClient restClient(final BotBuilderEngineApiProperties properties) {
        final var factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(properties.getReadTimeout());

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .requestInterceptor(RestClientConfig::logInterceptor)
                .build();
    }

    @Bean("emailApiRestClient")
    public RestClient emailApiRestClient(final EmailApiProperties properties) {
        final var factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(properties.getReadTimeout());

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .requestInterceptor(RestClientConfig::logInterceptor)
                .build();
    }

    private static ClientHttpResponse logInterceptor(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.info("[%s] %s".formatted(request.getMethod(), request.getURI().toString()));
        return execution.execute(request, body);
    }
}
