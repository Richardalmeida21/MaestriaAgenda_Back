package com.maestria.agenda.agendamento;

import java.time.LocalDate;
import java.time.LocalTime;
import com.maestria.agenda.agendamento.AgendamentoFixo.TipoRepeticao;

public record DadosCadastroAgendamentoFixo(
        Long clienteId,
        Long profissionalId,
        Long servicoId,
        LocalTime hora,
        LocalDate dataInicio,
        LocalDate dataFim,
        TipoRepeticao tipoRepeticao,
        int intervaloRepeticao,
        int valorRepeticao,
        String observacao,
        Integer diaDoMes,
        String formaPagamento
) {
    public DadosCadastroAgendamentoFixo {
        if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
            throw new IllegalArgumentException("A forma de pagamento é obrigatória.");
        }

        if (tipoRepeticao == TipoRepeticao.MENSAL && diaDoMes == null) {
            diaDoMes = valorRepeticao;
        }
    }
}
