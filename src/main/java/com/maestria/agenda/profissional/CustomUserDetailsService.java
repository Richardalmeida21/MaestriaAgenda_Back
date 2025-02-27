package com.maestria.agenda.profissional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

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
        UserBuilder builder = User.withUsername(username);
        builder.password(profissional.getSenha());
        builder.roles("PROFISSIONAL");
        return builder.build();
    }
}