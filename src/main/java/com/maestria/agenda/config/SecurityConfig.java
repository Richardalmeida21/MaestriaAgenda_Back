@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable()) // Desabilita CSRF
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/login", "/auth/register").permitAll() // 🔥 Garantir que login e registro sejam públicos
            .requestMatchers("/auth/user").authenticated() // 🔒 Apenas usuários logados podem acessar /auth/user
            .requestMatchers("/agendamento").hasAnyAuthority("ADMIN", "PROFISSIONAL")
            .requestMatchers("/cliente/**", "/profissional/**").hasAuthority("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
