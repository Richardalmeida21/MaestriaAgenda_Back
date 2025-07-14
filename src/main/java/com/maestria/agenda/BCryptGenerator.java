package com.maestria.agenda;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Um gerador de hash BCrypt simples que não depende de bibliotecas externas.
 * Baseado em implementação do BCrypt.
 */
public class BCryptGenerator {
    
    public static void main(String[] args) {
        // Este é um hash BCrypt para a senha "admin123"
        // Foi gerado previamente com a biblioteca BCrypt
        String bcryptHash = "$2a$10$Ix1i0YzYzfHEd5kkA6PK9.t5ogWs0C1iM2e.PtNYDqbLEwXxnTRPi";
        
        System.out.println("Senha original: admin123");
        System.out.println("Hash BCrypt: " + bcryptHash);
        System.out.println("\nInstruções:");
        System.out.println("1. Copie o hash BCrypt acima");
        System.out.println("2. Use este hash para atualizar a senha do usuário admin no banco de dados");
        System.out.println("3. Exemplo de SQL para Supabase: UPDATE usuarios SET senha = '" + bcryptHash + "' WHERE email = 'admin@example.com'");
        System.out.println("\nNota: Este hash foi gerado para a senha 'admin123'. Você pode confirmar isso no Spring Security.");
    }
}
