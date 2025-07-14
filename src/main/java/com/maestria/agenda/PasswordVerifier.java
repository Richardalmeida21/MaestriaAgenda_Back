package com.maestria.agenda;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitário para verificar se uma senha corresponde a um hash BCrypt.
 */
public class PasswordVerifier {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Senha em texto plano
        String rawPassword = "admin123";
        
        // Hash BCrypt que deve estar armazenado no banco de dados
        String storedHash = "$2a$10$Ix1i0YzYzfHEd5kkA6PK9.t5ogWs0C1iM2e.PtNYDqbLEwXxnTRPi";
        
        // Verifica se a senha corresponde ao hash
        boolean matches = encoder.matches(rawPassword, storedHash);
        
        System.out.println("Senha em texto plano: " + rawPassword);
        System.out.println("Hash BCrypt armazenado: " + storedHash);
        System.out.println("A senha corresponde ao hash? " + matches);
        
        if (matches) {
            System.out.println("\nA senha e o hash ESTÃO corretos!");
            System.out.println("Se o login ainda está falhando, o problema pode ser:");
            System.out.println("1. O hash não foi atualizado corretamente no banco de dados");
            System.out.println("2. O usuário 'admin' não existe ou está com outro login");
            System.out.println("3. Há algum problema na lógica de autenticação do backend");
        } else {
            System.out.println("\nERRO: A senha não corresponde ao hash!");
            System.out.println("Isso indica um problema no algoritmo de hash ou nos dados fornecidos.");
        }
        
        // Gera um novo hash para a mesma senha para demonstrar que BCrypt gera hashes diferentes cada vez
        String newHash = encoder.encode(rawPassword);
        System.out.println("\nNovo hash gerado para a mesma senha: " + newHash);
        System.out.println("Este novo hash também corresponde à senha? " + encoder.matches(rawPassword, newHash));
        
        // Dicas para SQL
        System.out.println("\nDicas para SQL no Supabase:");
        System.out.println("1. Verificar tabelas existentes:");
        System.out.println("   SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';");
        System.out.println("2. Verificar o usuário admin:");
        System.out.println("   SELECT * FROM profissional WHERE login = 'admin';");
        System.out.println("3. Atualizar a senha (usando o nome de tabela correto):");
        System.out.println("   UPDATE profissional SET senha = '" + storedHash + "' WHERE login = 'admin';");
    }
}
