package com.maestria.agenda;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitário para gerar hashes BCrypt para senhas.
 * Execute esta classe diretamente para gerar o hash para a senha especificada.
 */
public class PasswordEncoderUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // A senha que você deseja codificar
        String rawPassword = "admin123";
        
        // Gera o hash BCrypt
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("Senha original: " + rawPassword);
        System.out.println("Hash BCrypt gerado: " + encodedPassword);
        System.out.println("\nInstruções:");
        System.out.println("1. Copie o hash BCrypt gerado acima");
        System.out.println("2. Use este hash para atualizar a senha do usuário admin no banco de dados");
        System.out.println("3. Exemplo de SQL para Supabase: UPDATE usuarios SET senha = 'hash-bcrypt-copiado' WHERE email = 'admin@example.com'");
    }
    
    /**
     * Método utilitário para codificar uma senha
     * @param rawPassword A senha em texto puro
     * @return A senha codificada em BCrypt
     */
    public static String encodePassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(rawPassword);
    }
}