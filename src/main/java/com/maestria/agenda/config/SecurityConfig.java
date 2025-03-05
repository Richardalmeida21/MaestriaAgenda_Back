package com.maestria.agenda.config;

import com.maestria.agenda.profissional.CustomUserDetailsService;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Configuração de CORS
            .csrf(csrf -> csrf.disable())  // Desativando CSRF pois usamos JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // API Stateless, sem sessão
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register", "/public/**", "/generate-password").permitAll()  // Rotas públicas
                .requestMatchers("/profissional/teste-login/**").permitAll()  // Libera apenas o teste de login para todos
                .requestMatchers("/auth/me", "/agendamento").hasAnyAuthority("ADMIN", "PROFISSIONAL")  // Apenas ADMIN e PROFISSIONAL podem acessar
                .requestMatchers("/cliente/**").hasAuthority("ADMIN")  // Apenas ADMIN pode acessar clientes
                .requestMatchers("/profissional/**").hasAnyAuthority("ADMIN", "PROFISSIONAL")  // ADMIN e PROFISSIONAL podem acessar profissional
                .anyRequest().authenticated()  // Todas as outras rotas precisam de autenticação
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // Adiciona filtro JWT

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();  // Gerenciamento de autenticação
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Senhas criptografadas com BCrypt
    }

    @Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOrigins(List.of("http://localhost:8080", "https://maestria-agenda.netlify.app")); // Certifique-se de usar a porta correta
    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    corsConfig.setAllowedHeaders(List.of("*"));  // Permitir todos os cabeçalhos
    corsConfig.setExposedHeaders(List.of("Authorization"));
    corsConfig.setAllowCredentials(false); // Se permitir "*", precisa ser false

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);
    return source;
}

}
