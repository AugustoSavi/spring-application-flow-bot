package com.flowbot.application.module.domain.transacao;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.flowbot.application.utils.Utils.nullOrToday;
import static com.flowbot.application.utils.Utils.nullOrValue;

@Document
public class Transacao {
    @Id
    private String id;
    private String externalReference;
    private String resourceOwner;
    private BigDecimal valor;
    private String devedorNome;
    private String devedorCPF;
    private String descricaoSolicitacao;
    private StatusTransacao status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime dataCriacao;
    private String qrCode;
    private String pixCopiaECola;

    public Transacao(String id,
                     String externalReference,
                     String resourceOwner,
                     BigDecimal valor,
                     String devedorNome,
                     String devedorCPF,
                     String descricaoSolicitacao,
                     StatusTransacao status,
                     LocalDateTime dataCriacao) {
        this.id = id;
        this.externalReference = externalReference;
        this.resourceOwner = resourceOwner;
        this.valor = valor;
        this.devedorNome = devedorNome;
        this.devedorCPF = devedorCPF;
        this.descricaoSolicitacao = descricaoSolicitacao;
        this.status = (StatusTransacao) nullOrValue(status, StatusTransacao.SOLICITADO);
        this.dataCriacao = nullOrToday(dataCriacao);
    }

    public String getId() {
        return id;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public String getResourceOwner() {
        return resourceOwner;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDevedorNome() {
        return devedorNome;
    }

    public String getDevedorCPF() {
        return devedorCPF;
    }

    public String getDescricaoSolicitacao() {
        return descricaoSolicitacao;
    }

    public StatusTransacao getStatus() {
        return status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void atualizarStatus(final StatusTransacao novoStatus) {
        this.status = novoStatus;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getPixCopiaECola() {
        return pixCopiaECola;
    }

    public void atualizarDadosPix(String qrCode, String pixCopiaECola) {
        this.qrCode = qrCode;
        this.pixCopiaECola = pixCopiaECola;
    }
}
