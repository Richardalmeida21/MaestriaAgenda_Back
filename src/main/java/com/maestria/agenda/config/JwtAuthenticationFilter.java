package com.maestria.agenda.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String SECRET_KEY = "your_secret_key"; // Substitua pela chave secreta usada para gerar os tokens JWT.
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        // Obter o token do cabeçalho Authorization
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // Remover o prefixo "Bearer "

            try {
                // Verificar e decodificar o token
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                User userDetails = (User) userDetailsService.loadUserByUsername(username);

                // Verificar se a autenticação é válida
                if (username != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, Collections.emptyList()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                // Se o token for inválido ou expirar, apenas ignore e continue
            }
        }

        filterChain.doFilter(request, response);
    }
}
