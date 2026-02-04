package com.maestria.agenda.profissional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Serviço com cache para Profissionais
 * Reduz queries ao banco de dados
 */
@Service
public class ProfissionalCacheService {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    /**
     * Busca profissional por ID com cache
     * Cache é mantido até que o profissional seja atualizado
     */
    @Cacheable(value = "profissionais", key = "#id")
    public Optional<Profissional> findById(Long id) {
        return profissionalRepository.findById(id);
    }

    /**
     * Lista todos os profissionais com cache
     */
    @Cacheable(value = "profissionais", key = "'all'")
    public List<Profissional> findAll() {
        return profissionalRepository.findAll();
    }

    /**
     * Busca profissional por login com cache
     */
    @Cacheable(value = "profissionais", key = "'login:' + #login")
    public Profissional findByLogin(String login) {
        return profissionalRepository.findByLogin(login);
    }

    /**
     * Limpa o cache quando um profissional é atualizado
     */
    @CacheEvict(value = "profissionais", allEntries = true)
    public Profissional save(Profissional profissional) {
        return profissionalRepository.save(profissional);
    }

    /**
     * Limpa o cache quando um profissional é deletado
     */
    @CacheEvict(value = "profissionais", allEntries = true)
    public void deleteById(Long id) {
        profissionalRepository.deleteById(id);
    }
}
