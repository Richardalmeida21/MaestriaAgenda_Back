package com.maestria.agenda.profissional;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        Optional<Profissional> profissionalOpt = Optional.ofNullable(profissionalRepository.findByLogin(loginRequest.getUsername()));

        if (profissionalOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Usuário não encontrado.");
        }

        Profissional profissional = profissionalOpt.get();
        if (!passwordEncoder.matches(loginRequest.getSenha(), profissional.getSenha())) {
            return ResponseEntity.status(401).body("Senha incorreta.");
        }

        // Gerar Token JWT ✅
        String token = Jwts.builder()
                .setSubject(loginRequest.getUsername())
                .claim("role", profissional.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
}
