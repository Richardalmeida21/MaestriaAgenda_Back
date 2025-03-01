package com.maestria.agenda.profissional;

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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
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
            // 🔹 Verificação de campos vazios
            if (loginRequest.getUsername() == null || loginRequest.getSenha() == null) {
                return ResponseEntity.badRequest().body("Username e senha não podem estar vazios.");
            }

            // 🔹 Buscar usuário no banco
            Profissional profissional = profissionalRepository.findByLogin(loginRequest.getUsername());
            if (profissional == null) {
                return ResponseEntity.status(401).body("Usuário não encontrado.");
            }

            // 🔹 Verificar se a senha está correta
            if (!passwordEncoder.matches(loginRequest.getSenha(), profissional.getSenha())) {
                return ResponseEntity.status(401).body("Senha incorreta.");
            }

            // 🔹 Autenticar usuário no Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getSenha())
            );

            // 🔹 Gerar Token JWT
            String token = Jwts.builder()
                    .setSubject(loginRequest.getUsername())
                    .claim("role", "PROFISSIONAL")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Expira em 1 dia
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();

            // 🔹 Retornar token
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
