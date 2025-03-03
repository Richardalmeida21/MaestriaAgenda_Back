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
