package com.flowbot.application.configs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@ConditionalOnExpression("${keycloak.enabled:true} == true")
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(
                        authorizeConfig -> {
                            authorizeConfig.requestMatchers("/ws", "/ws/**").permitAll();
                            authorizeConfig.requestMatchers("/public").permitAll();
                            authorizeConfig.requestMatchers("/logout").permitAll();
                            authorizeConfig.requestMatchers(HttpMethod.GET,"/plano/vigente").permitAll();
                            authorizeConfig.requestMatchers(HttpMethod.POST, "/plano/acesso").permitAll();
                            authorizeConfig.requestMatchers(HttpMethod.GET, "/configuracoes-usuario/public/**").permitAll();
                            authorizeConfig.requestMatchers(HttpMethod.POST, "/usuario").permitAll();
                            authorizeConfig.requestMatchers(HttpMethod.GET, "/usuario/confirmar").permitAll();
                            authorizeConfig.requestMatchers(HttpMethod.OPTIONS).permitAll();
                            authorizeConfig.anyRequest().authenticated();
                        })
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2Login(Customizer.withDefaults())
                .oauth2ResourceServer(conf -> conf.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}