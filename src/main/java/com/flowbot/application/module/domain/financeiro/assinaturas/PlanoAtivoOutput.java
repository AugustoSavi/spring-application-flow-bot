package com.flowbot.application.module.domain.financeiro.assinaturas;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record PlanoAtivoOutput(
        String email,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate vigenteAte,
        Boolean gratuito
) {

    public static PlanoAtivoOutput map(Plano plano) {
        return new PlanoAtivoOutput(
                plano.getUsuario().email(),
                plano.getFinalizaEm().toLocalDate(),
                plano.getGratuito()
        );
    }
}
