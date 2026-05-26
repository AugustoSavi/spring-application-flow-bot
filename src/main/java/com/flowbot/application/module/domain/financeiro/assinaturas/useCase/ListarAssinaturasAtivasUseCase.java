package com.flowbot.application.module.domain.financeiro.assinaturas.useCase;

import com.flowbot.application.module.domain.financeiro.assinaturas.Plano;
import com.flowbot.application.module.domain.financeiro.assinaturas.api.dto.AssinaturaAtivaDto;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
public class ListarAssinaturasAtivasUseCase {
    private final MongoTemplate mongoTemplate;

    public ListarAssinaturasAtivasUseCase(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<AssinaturaAtivaDto> listar() {
        LocalDate hoje = LocalDate.now();

        Query query = new Query()
                .addCriteria(Criteria.where("ativo").is(true));
        List<Plano> planos = mongoTemplate.find(query, Plano.class);

        return planos.stream()
                .map(plano -> {
                    String usuario = plano.getUsuario().nick() != null && !plano.getUsuario().nick().isEmpty()
                            ? plano.getUsuario().nick()
                            : plano.getUsuario().email();
                    LocalDate dataCriacao = plano.getDataCriacao().toLocalDate();
                    long diasInscrito = ChronoUnit.DAYS.between(dataCriacao, hoje);
                    String ativoHa = formatarTempoInscrito(dataCriacao, hoje);
                    return new AssinaturaAtivaItem(usuario, ativoHa, diasInscrito);
                })
                .sorted(Comparator.comparingLong(AssinaturaAtivaItem::diasInscrito).reversed())
                .map(i -> new AssinaturaAtivaDto(i.usuario(), i.ativoHa()))
                .toList();
    }

    private record AssinaturaAtivaItem(String usuario, String ativoHa, long diasInscrito) {}

    private String formatarTempoInscrito(LocalDate dataCriacao, LocalDate hoje) {
        Period period = Period.between(dataCriacao, hoje);
        int anos = period.getYears();
        int meses = period.getMonths();
        int dias = period.getDays();

        if (anos > 0) {
            if (meses > 0) {
                return anos + (anos == 1 ? " ano e " : " anos e ") + meses + (meses == 1 ? " mês" : " meses");
            }
            return anos + (anos == 1 ? " ano" : " anos");
        }
        if (meses > 0) {
            if (dias > 0) {
                return meses + (meses == 1 ? " mês e " : " meses e ") + dias + (dias == 1 ? " dia" : " dias");
            }
            return meses + (meses == 1 ? " mês" : " meses");
        }
        return dias + (dias == 1 ? " dia" : " dias");
    }
}
