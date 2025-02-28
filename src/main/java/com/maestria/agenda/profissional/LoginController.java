package com.maestria.agenda.profissional;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    private final String SECRET_KEY = "seuSegredoSuperSeguro";  // Use um segredo forte

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getSenha())
            );

            // Criando um token JWT
            String token = Jwts.builder()
                    .setSubject(loginRequest.getUsername()) // Setando o nome de usuário como o sujeito do token
                    .setIssuedAt(new Date())  // Data de emissão do token
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Expiração em 1 dia
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // Assinando o token com a chave secreta
                    .compact();

            Map<String, String> response = new HashMap<>();
            response.put("token", token); // Retorna o token JWT

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Login failed");
        }
    }
}
