package com.maestria.agenda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Correção da importação

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http
            .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // Filtra a autenticação com JWT
            .cors().and()
            .csrf().disable()  // Desabilitar CSRF
            .authorizeRequests(authz -> authz
                .requestMatchers("/public/**", "/login", "/register").permitAll()  // Permite acesso sem autenticação
                .anyRequest().authenticated()  // Requer autenticação para outras requisições
            )
            .formLogin().disable()  // Desabilita o login tradicional de formulário
            .httpBasic().disable(); // Se não for utilizar autenticação básica HTTP

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
