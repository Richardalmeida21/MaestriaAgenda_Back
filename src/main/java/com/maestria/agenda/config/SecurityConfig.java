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
        corsConfig.setAllowedOrigins(Arrays.asList("https://maestria-agenda.netlify.app")); // Origem do frontend
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Métodos permitidos
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // Cabeçalhos permitidos

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Aplica a configuração para todas as rotas

        return source;
    }
}
