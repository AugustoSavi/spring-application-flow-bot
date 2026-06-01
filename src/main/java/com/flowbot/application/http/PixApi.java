package com.flowbot.application.http;

import com.flowbot.application.http.dtos.CriarCobrancaInput;

public interface PixApi {

    void criarCobranca(CriarCobrancaInput input);
}
