package com.maestria.agenda.servico;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Serviço com cache para Serviços
 * Reduz queries ao banco de dados
 */
@Service
public class ServicoCacheService {

    @Autowired
    private ServicoRepository servicoRepository;

    /**
     * Busca serviço por ID com cache
     */
    @Cacheable(value = "servicos", key = "#id")
    public Optional<Servico> findById(Long id) {
        return servicoRepository.findById(id);
    }

    /**
     * Lista todos os serviços com cache
     */
    @Cacheable(value = "servicos", key = "'all'")
    public List<Servico> findAll() {
        return servicoRepository.findAll();
    }

    /**
     * Lista serviços ativos com cache separado
     * Este é o mais usado, então tem cache próprio
     */
    @Cacheable(value = "servicosAtivos", key = "'active'")
    public List<Servico> findByAtivoTrue() {
        return servicoRepository.findByAtivoTrue();
    }

    /**
     * Limpa o cache quando um serviço é atualizado
     */
    @CacheEvict(value = { "servicos", "servicosAtivos" }, allEntries = true)
    public Servico save(Servico servico) {
        return servicoRepository.save(servico);
    }

    /**
     * Limpa o cache quando um serviço é deletado
     */
    @CacheEvict(value = { "servicos", "servicosAtivos" }, allEntries = true)
    public void deleteById(Long id) {
        servicoRepository.deleteById(id);
    }
}
