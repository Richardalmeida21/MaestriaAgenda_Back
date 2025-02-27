package com.maestria.agenda.controller;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.agendamento.DadosCadastroAgendamento;
import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.cliente.ClienteRepository;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/agendamento")
public class AgendamentoController {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    public AgendamentoController(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository, ProfissionalRepository profissionalRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    @PostMapping
    public void cadastrar(@RequestBody DadosCadastroAgendamento dados) {
        // 1. Salva o Cliente
        Cliente cliente = new Cliente();
        cliente.setNome(dados.cliente().nome());
        cliente.setEmail(dados.cliente().email());
        cliente.setTelefone(dados.cliente().telefone());
        clienteRepository.save(cliente);

        // 2. Busca o Profissional pelo nome (retorna uma lista de profissionais)
        String nomeProfissional = dados.profissional().nome();
        List<Profissional> profissionais = profissionalRepository.findAllByNome(nomeProfissional);

        Profissional profissional;
        if (profissionais.isEmpty()) {
            // Se não encontrar, cria um novo
            profissional = new Profissional();
            profissional.setNome(nomeProfissional);
            profissional.setLogin("defaultLogin"); // Defina um valor padrão ou obtenha de `dados`
            profissional.setSenha("defaultSenha"); // Defina um valor padrão ou obtenha de `dados`
            profissionalRepository.save(profissional);
        } else {
            // Se encontrar, escolhe o primeiro da lista
            profissional = profissionais.get(0);
        }

        // 3. Salva o Agendamento com o Cliente e o Profissional
        Agendamento agendamento = new Agendamento(dados, cliente, profissional);
        agendamentoRepository.save(agendamento);
    }

    @GetMapping
    public List<Agendamento> listarAgendamentos() {
        return agendamentoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> buscarAgendamentoPorId(@PathVariable Long id) {
        Optional<Agendamento> agendamento = agendamentoRepository.findById(id);
        return agendamento.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> atualizarAgendamento(@PathVariable Long id, @RequestBody DadosCadastroAgendamento dados) {
        Optional<Agendamento> agendamentoExistente = agendamentoRepository.findById(id);

        if (agendamentoExistente.isPresent()) {
            Agendamento agendamento = agendamentoExistente.get();

            // Atualiza o cliente - Se o cliente já existir, apenas atualiza seus dados
            Cliente cliente = agendamento.getCliente(); // Obtém o cliente associado ao agendamento

            // Se o cliente não existir (provavelmente nunca deveria ser o caso, mas é bom garantir)
            if (cliente == null) {
                cliente = new Cliente();
            }

            cliente.setNome(dados.cliente().nome());
            cliente.setEmail(dados.cliente().email());
            cliente.setTelefone(dados.cliente().telefone());
            clienteRepository.save(cliente);  // Salva ou atualiza o cliente

            // Atualiza o profissional - Cria ou busca um profissional com o nome
            String nomeProfissional = dados.profissional().nome();
            List<Profissional> profissionais = profissionalRepository.findAllByNome(nomeProfissional);
            Profissional profissional;

            if (profissionais.isEmpty()) {
                // Se não encontrar, cria um novo
                profissional = new Profissional();
                profissional.setNome(nomeProfissional);
                profissional.setLogin("defaultLogin"); // Defina um valor padrão ou obtenha de `dados`
                profissional.setSenha("defaultSenha"); // Defina um valor padrão ou obtenha de `dados`
                profissionalRepository.save(profissional);
            } else {
                // Se encontrar, usa o primeiro da lista
                profissional = profissionais.get(0);
            }

            // Atualiza os dados do agendamento
            agendamento.setCliente(cliente);  // Atualiza o cliente
            agendamento.setProfissional(profissional);  // Atualiza o profissional
            agendamento.setServico(dados.servico());  // Atualiza o serviço
            agendamento.setData(dados.data());  // Atualiza a data
            agendamento.setHora(dados.hora());  // Atualiza a hora

            // Salva o agendamento atualizado
            agendamentoRepository.save(agendamento);

            // Retorna o agendamento atualizado como resposta
            return ResponseEntity.ok(agendamento);
        }

        // Caso não encontre o agendamento com o ID fornecido
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAgendamento(@PathVariable Long id) {
        Optional<Agendamento> agendamentoExistente = agendamentoRepository.findById(id);
        if (agendamentoExistente.isPresent()) {
            agendamentoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build(); // Se não encontrar o agendamento
    }
}