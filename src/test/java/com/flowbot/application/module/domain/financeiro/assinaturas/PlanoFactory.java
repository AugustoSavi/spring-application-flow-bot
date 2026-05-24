package com.flowbot.application.module.domain.financeiro.assinaturas;

import java.time.LocalDateTime;

public final class PlanoFactory {

    public static Plano umPlanoMensal(String email) {
        return Plano.criarPlanoPadrao(email, PeriodoPlano.MENSAL);
    }

    public static Plano umPlanoPagoVencido(String email) {
        return umPlanoVencido(email, false);
    }

    public static Plano umPlanoGratuitoVencido(String email) {
        return umPlanoVencido(email, true);
    }

    private static Plano umPlanoVencido(String email, boolean gratuito) {
        var plano = Plano.criarPlano(email, PeriodoPlano.MENSAL, gratuito);
        try {
            var field = Plano.class.getDeclaredField("finalizaEm");
            field.setAccessible(true);
            field.set(plano, LocalDateTime.now().minusDays(1));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return plano;
    }
}
