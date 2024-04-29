package com.flowbot.application.module.domain.numeros.api.dto;

import com.flowbot.application.module.domain.numeros.Numero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public final class DtoUtils {

    public static Page<NumeroOutput> toDto(Page<Numero> page) {
        Pageable pageable = page.getPageable();
        var content = listToDto(page.getContent());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    private static List<NumeroOutput> listToDto(List<Numero> content) {
        return content.stream().map(DtoUtils::toObject).toList();
    }

    private static NumeroOutput toObject(Numero numero) {
        return new NumeroOutput(
                numero.getId(),
                numero.getNick(),
                numero.getNumero()
        );
    }
}