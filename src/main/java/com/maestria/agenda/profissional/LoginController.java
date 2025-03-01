package com.maestria.agenda.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import com.maestria.agenda.profissional.LoginRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Base64;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("🔍 Tentando login para usuário: " + loginRequest.getUsername());

        try {
            // 🔹 Verificar se os campos foram enviados corretamente
            if (loginRequest.getUsername() == null || loginRequest.getSenha() == null) {
                System.out.println("❌ Erro: Usuário ou senha estão vazios!");
                return ResponseEntity.badRequest().body("Usuário e senha não podem estar vazios.");
            }

            // 🔹 Buscar o usuário no banco de dados
            Optional<Profissional> profissionalOpt = Optional.ofNullable(profissionalRepository.findByLogin(loginRequest.getUsername()));
            if (profissionalOpt.isEmpty()) {
                System.out.println("❌ Usuário não encontrado!");
                return ResponseEntity.status(401).body("Usuário não encontrado.");
            }

            Profissional profissional = profissionalOpt.get();
            System.out.println("✅ Usuário encontrado: " + profissional.getLogin());

            // 🔹 Verificar se a senha está correta
            if (!passwordEncoder.matches(loginRequest.getSenha(), profissional.getSenha())) {
                System.out.println("❌ Senha incorreta!");
                return ResponseEntity.status(401).body("Senha incorreta.");
            }

            System.out.println("✅ Senha correta, gerando token...");

            // 🔹 Gerar Token JWT
            String token = Jwts.builder()
                    .setSubject(loginRequest.getUsername())
                    .claim("role", "PROFISSIONAL")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token válido por 24h
                    .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(SECRET_KEY.getBytes()))
                    .compact();

            // 🔹 Retornar token
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            System.out.println("✅ Login bem-sucedido! Token gerado.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Erro interno no servidor: " + e.getMessage());
            return ResponseEntity.status(500).body("Erro interno no servidor.");
        }
    }
}
