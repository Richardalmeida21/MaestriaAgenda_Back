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
        Integer diaDoMes
) {
    // Manter o construtor original para compatibilidade
    public DadosCadastroAgendamentoFixo {
        // Se for agendamento mensal e não tiver dia do mês especificado, use o valor de repetição
        if (tipoRepeticao == TipoRepeticao.MENSAL && diaDoMes == null) {
            diaDoMes = valorRepeticao;
        }
    }
}