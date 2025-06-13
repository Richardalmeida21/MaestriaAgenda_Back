package com.maestria.agenda.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String senhaCriptografada = encoder.encode("senha123");
        System.out.println("Senha criptografada: " + senhaCriptografada);
        
        // Verificar se a senha está correta
        boolean matches = encoder.matches("senha123", senhaCriptografada);
        System.out.println("Senha verifica corretamente: " + matches);
    }
} 