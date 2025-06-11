package com.maestria.agenda.agendamento;

import java.time.LocalDate;
import java.time.LocalTime;
import com.maestria.agenda.agendamento.AgendamentoFixo.TipoRepeticao;
import com.maestria.agenda.financeiro.PagamentoTipo;


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
    public DadosCadastroAgendamentoFixo {
        if (tipoRepeticao == TipoRepeticao.MENSAL && diaDoMes == null) {
            diaDoMes = valorRepeticao;
        }
    }
}
