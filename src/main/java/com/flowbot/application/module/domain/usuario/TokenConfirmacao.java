package com.flowbot.application.module.domain.usuario;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document("token_confirmacao")
public class TokenConfirmacao {

    @Id
    private String token;
    private String email;
    private LocalDateTime expiraEm;

    public static TokenConfirmacao criar(String email) {
        return criar(email, LocalDateTime.now().plusHours(24));
    }

    public static TokenConfirmacao criar(String email, LocalDateTime expiraEm) {
        var t = new TokenConfirmacao();
        t.token = UUID.randomUUID().toString();
        t.email = email;
        t.expiraEm = expiraEm;
        return t;
    }

    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(expiraEm);
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }
}
