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
        System.out.println("🔍 Tentando login para usuário: " + loginRequest.getUsername());

        try {
            // 🔹 Verificar se os campos foram enviados corretamente
            if (loginRequest.getUsername() == null || loginRequest.getSenha() == null) {
                return ResponseEntity.badRequest().body("Username e senha não podem estar vazios.");
            }

            // 🔹 Buscar o usuário no banco de dados
            Profissional profissional = profissionalRepository.findByLogin(loginRequest.getUsername());
            if (profissional == null) {
                System.out.println("❌ Usuário não encontrado!");
                return ResponseEntity.status(401).body("Usuário não encontrado.");
            }

            System.out.println("✅ Usuário encontrado: " + profissional.getLogin());
            System.out.println("🔐 Senha digitada: " + loginRequest.getSenha());
            System.out.println("🔐 Senha armazenada: " + profissional.getSenha());

            // 🔹 Verificar se a senha está correta
            if (!passwordEncoder.matches(loginRequest.getSenha(), profissional.getSenha())) {
                System.out.println("❌ Senha incorreta!");
                return ResponseEntity.status(401).body("Senha incorreta.");
            }

            System.out.println("✅ Senha correta, gerando token...");

            // 🔹 Autenticar usuário no Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getSenha())
            );

            // 🔹 Gerar Token JWT
            String token = Jwts.builder()
                    .setSubject(loginRequest.getUsername())
                    .claim("role", "PROFISSIONAL")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();

            // 🔹 Retornar token
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            System.out.println("✅ Login bem-sucedido! Token gerado.");
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            System.out.println("❌ Erro: Credenciais inválidas.");
            return ResponseEntity.status(401).body("Credenciais inválidas.");
        } catch (Exception e) {
            System.out.println("❌ Erro interno: " + e.getMessage());
            return ResponseEntity.status(500).body("Erro interno no servidor.");
        }
    }
}
