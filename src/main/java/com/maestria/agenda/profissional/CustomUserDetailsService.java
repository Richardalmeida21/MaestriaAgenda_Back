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
        Profissional profissional = profissionalRepository.findByLogin(username);
        if (profissional == null) {
            throw new UsernameNotFoundException("Profissional não encontrado");
        }

        return new User(
                profissional.getLogin(),
                profissional.getSenha(),
                Collections.singletonList(new SimpleGrantedAuthority(profissional.getRole()))
        );
    }
}