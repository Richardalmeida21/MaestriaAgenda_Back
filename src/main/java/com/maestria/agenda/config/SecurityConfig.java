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
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS configuração
            .csrf(csrf -> csrf.disable())  // Desabilitar CSRF já que estamos usando JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Stateless, não há sessão no backend
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register", "/public/**", "/generate-password").permitAll()  // Acesso público
                .requestMatchers("/auth/me", "/agendamento").hasAnyAuthority("ADMIN", "PROFISSIONAL")  // Requer LOGIN de ADMIN ou PROFISSIONAL
                .requestMatchers("/cliente/**", "/profissional/**").hasAuthority("ADMIN")  // Apenas ADMIN pode acessar rotas de cliente/profissional
                .anyRequest().authenticated()  // Requer autenticação para outras rotas
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // Filtro JWT antes da autenticação padrão

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();  // Gerenciamento de autenticação
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Criptografar senhas usando BCrypt
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(List.of("https://maestria-agenda.netlify.app"));  // Frontend hospedado no Netlify
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("Authorization", "Content-Type"));  // Cabeçalhos permitidos
        corsConfig.setExposedHeaders(List.of("Authorization"));  // Expor o cabeçalho Authorization para o frontend
        corsConfig.setAllowCredentials(true);  // Permitir credenciais (como cookies, tokens)

        // Definir as configurações de CORS para todas as URLs
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}
