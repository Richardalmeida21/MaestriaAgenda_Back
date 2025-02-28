package com.maestria.agenda.profissional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Injeção do PasswordEncoder

    @Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Profissional profissional = profissionalRepository.findByLogin(username);
    if (profissional == null) {
        throw new UsernameNotFoundException("Profissional não encontrado");
    }

    if (!passwordEncoder.matches(profissional.getSenha(), profissional.getSenha())) {
        throw new UsernameNotFoundException("Senha inválida");
    }

    UserBuilder builder = User.withUsername(username);
    builder.password(profissional.getSenha());  // A senha já está criptografada
    builder.roles("PROFISSIONAL");
    return builder.build();
}

}
