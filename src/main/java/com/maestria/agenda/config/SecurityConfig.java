package com.maestria.agenda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Habilita o CORS
            .csrf(csrf -> csrf.disable()) // Desativa CSRF para APIs stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API stateless
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register").permitAll() // 🔓 Login e Cadastro Liberados
                .requestMatchers("/auth/user").authenticated() // 🔒 Apenas usuários autenticados
                .requestMatchers("/agendamento").hasAnyAuthority("ADMIN", "PROFISSIONAL") // 🔒 Apenas admin e profissionais
                .requestMatchers("/cliente/**", "/profissional/**").hasAuthority("ADMIN") // 🔒 Apenas ADMIN
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // Adiciona o filtro JWT

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Codifica senhas
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of("https://maestria-agenda.netlify.app")); // Frontend URL
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));  // Métodos permitidos
        corsConfig.setAllowedHeaders(List.of("Authorization", "Content-Type"));  // Cabeçalhos permitidos
        corsConfig.setExposedHeaders(List.of("Authorization"));  // Expondo o cabeçalho Authorization
        corsConfig.setAllowCredentials(true);  // Permite o envio de credenciais (cookies, autenticação)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);  // Aplica configuração para todos os endpoints
        return source;
    }
}
