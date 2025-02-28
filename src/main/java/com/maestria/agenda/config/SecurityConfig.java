package com.maestria.agenda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors().configurationSource(corsConfigurationSource()) // Ativa a configuração de CORS
            .and()
            .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // Filtra a autenticação com JWT
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

    // Configuração de CORS para permitir origens externas
    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost", "https://mastriaagenda-production.up.railway.app")); // Configura as origens permitidas
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Configura os métodos permitidos
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // Configura os cabeçalhos permitidos

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Aplica a configuração para todas as rotas

        return source;
    }

    // Criando o PasswordEncoder para ser injetado
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Usando o BCryptPasswordEncoder para segurança das senhas
    }
}
