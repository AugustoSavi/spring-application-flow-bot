package com.flowbot.application.module.domain.usuario;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;

@Document("usuario")
public class Usuario {
    @Id
    private String id;
    private String email;
    private String senhaHash;
    private Boolean emailValidado;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataCriacao;

    public static Usuario criar(String email, String senhaHash) {
        validar(email);
        var usuario = new Usuario();
        usuario.email = email;
        usuario.senhaHash = senhaHash;
        usuario.emailValidado = false;
        usuario.dataCriacao = LocalDateTime.now();
        return usuario;
    }

    private static void validar(String email) {
        if (Objects.isNull(email) || email.isBlank()) {
            throw new IllegalArgumentException("Email do usuário não pode ser nulo ou vazio");
        }
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Boolean getEmailValidado() {
        return emailValidado;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void confirmarEmail() {
        this.emailValidado = true;
    }
}
