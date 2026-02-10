package com.maestria.agenda.profissional;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // ✅ Permite requisições de qualquer origem (temporário para testes)
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("🔐 Tentativa de login - Username: {}", loginRequest.getUsername());
        
        Profissional profissional = profissionalRepository.findByLogin(loginRequest.getUsername());

        if (profissional == null) {
            logger.warn("❌ Login falhou - Usuário não encontrado: {}", loginRequest.getUsername());
            return ResponseEntity.status(401).body("Usuário não encontrado.");
        }

        logger.info("✅ Usuário encontrado: {} (ID: {})", profissional.getLogin(), profissional.getId());
        logger.debug("Senha recebida: {} | Hash no banco: {}", loginRequest.getSenha(), profissional.getSenha());
        
        boolean senhaCorreta = passwordEncoder.matches(loginRequest.getSenha(), profissional.getSenha());
        logger.info("Resultado da validação de senha: {}", senhaCorreta);

        if (!senhaCorreta) {
            logger.warn("❌ Login falhou - Senha incorreta para usuário: {}", loginRequest.getUsername());
            return ResponseEntity.status(401).body("Senha incorreta.");
        }

        logger.info("✅ Login bem-sucedido - Usuário: {}", loginRequest.getUsername());

        // 🔥 Gera token JWT usando uma chave segura
        String token = Jwts.builder()
                .setSubject(loginRequest.getUsername())
                .claim("role", profissional.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Expira em 24h
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
}
