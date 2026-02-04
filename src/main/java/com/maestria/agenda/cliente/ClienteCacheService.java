package com.maestria.agenda.cliente;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Serviço com cache para Clientes
 * Reduz queries ao banco de dados
 */
@Service
public class ClienteCacheService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Busca cliente por ID com cache
     */
    @Cacheable(value = "clientes", key = "#id")
    public Optional<Cliente> findById(Long id) {
        return clienteRepository.findById(id);
    }

    /**
     * Lista todos os clientes com cache
     */
    @Cacheable(value = "clientes", key = "'all'")
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    /**
     * Limpa o cache quando um cliente é atualizado
     */
    @CacheEvict(value = "clientes", allEntries = true)
    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    /**
     * Limpa o cache quando um cliente é deletado
     */
    @CacheEvict(value = "clientes", allEntries = true)
    public void deleteById(Long id) {
        clienteRepository.deleteById(id);
    }
}
