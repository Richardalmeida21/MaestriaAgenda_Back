package com.maestria.agenda.controller;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.agendamento.DadosCadastroAgendamento;
import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.cliente.ClienteRepository;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/agendamento")
@CrossOrigin(origins = "*")
public class AgendamentoController {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoController.class);

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    public AgendamentoController(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository, ProfissionalRepository profissionalRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody DadosCadastroAgendamento dados) {
        try {
            logger.info("Iniciando cadastro de agendamento");

            // Busca Cliente
            Optional<Cliente> clienteOpt = clienteRepository.findById(dados.clienteId());
            if (clienteOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Cliente não encontrado.");
            }
            Cliente cliente = clienteOpt.get();

            // Busca Profissional
            Optional<Profissional> profissionalOpt = profissionalRepository.findById(dados.profissionalId());
            if (profissionalOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Profissional não encontrado.");
            }
            Profissional profissional = profissionalOpt.get();

            // Cria e salva o agendamento
            Agendamento agendamento = new Agendamento(dados, cliente, profissional);
            agendamentoRepository.save(agendamento);
            logger.info("Agendamento salvo com sucesso: {}", agendamento);

            return ResponseEntity.ok("Agendamento criado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao criar agendamento", e);
            return ResponseEntity.status(500).body("Erro ao criar agendamento");
        }
    }

    @GetMapping
    public List<Agendamento> listarAgendamentos() {
        return agendamentoRepository.findAll();
    }
}
