package com.flowbot.application.http;

import com.flowbot.application.http.dtos.CriarCobrancaInput;
import com.flowbot.application.http.dtos.CriarCobrancaOutput;

public interface PixApi {

    CriarCobrancaOutput criarCobranca(CriarCobrancaInput input);
}
