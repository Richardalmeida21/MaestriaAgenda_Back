package com.maestria.agenda.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import com.maestria.agenda.profissional.LoginRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            if (loginRequest.getUsername() == null || loginRequest.getSenha() == null) {
                return ResponseEntity.badRequest().body("Usuário e senha não podem estar vazios.");
            }

            Profissional profissional = profissionalRepository.findByLogin(loginRequest.getUsername());
            if (profissional == null) {
                return ResponseEntity.status(401).body("Usuário não encontrado.");
            }

            if (!passwordEncoder.matches(loginRequest.getSenha(), profissional.getSenha())) {
                return ResponseEntity.status(401).body("Senha incorreta.");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getSenha())
            );

            String token = Jwts.builder()
                    .setSubject(loginRequest.getUsername())
                    .claim("role", "PROFISSIONAL")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Credenciais inválidas.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno no servidor.");
        }
    }
}
