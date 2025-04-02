package com.maestria.agenda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ping").permitAll()
                        .requestMatchers("/auth/login", "/auth/register", "/public/**", "/generate-password")
                        .permitAll()
                        .requestMatchers("/auth/me", "/agendamento/**").hasAnyAuthority("ADMIN", "PROFISSIONAL")
                        .requestMatchers("/cliente/**").hasAnyAuthority("ADMIN", "PROFISSIONAL")
                        .requestMatchers("/profissional/**").hasAnyAuthority("ADMIN", "PROFISSIONAL")
                        .requestMatchers("/servico/**").hasAnyAuthority("ADMIN", "PROFISSIONAL")
                        .requestMatchers("/bloqueio/**").hasAnyAuthority("ADMIN", "PROFISSIONAL")
                        .requestMatchers("/metricas").hasAuthority("ADMIN")
                        .requestMatchers("/agendamento/comissoes/total/**").hasAuthority("ADMIN")
                        .requestMatchers("/agendamento/fixo/**").hasAuthority("ADMIN")
                        .requestMatchers("/financeiro/comissoes").hasAuthority("ADMIN")
                        .requestMatchers("/financeiro/comissoes/profissional/**")
                        .hasAnyAuthority("ADMIN", "PROFISSIONAL")
                        .requestMatchers("/financeiro/comissoes/minhas").hasAuthority("PROFISSIONAL")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // Configuração para desenvolvimento e produção
        corsConfig.setAllowedOrigins(List.of("http://localhost:8080", "https://agendamaestria.vercel.app"));
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setExposedHeaders(List.of("Authorization"));
        corsConfig.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}