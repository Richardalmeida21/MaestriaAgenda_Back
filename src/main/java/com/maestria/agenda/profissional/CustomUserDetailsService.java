package com.maestria.agenda.profissional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscando o profissional pelo login
        Profissional profissional = profissionalRepository.findByLogin(username);
        
        // Se o profissional não for encontrado, lança uma exceção
        if (profissional == null) {
            throw new UsernameNotFoundException("Profissional com login '" + username + "' não encontrado.");
        }

        // Usando enum Role para garantir que apenas os valores válidos sejam passados
        return new User(
                profissional.getLogin(),                        // Login do profissional
                profissional.getSenha(),                        // Senha criptografada
                Collections.singletonList(new SimpleGrantedAuthority(profissional.getRole().name())) // Atribui a autoridade de role
        );
    }
}
