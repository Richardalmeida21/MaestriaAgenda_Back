package com.maestria.agenda.util;

import java.util.Arrays;

/**
 * Utilitário para verificar as variáveis de ambiente e configurações de conexão do banco de dados.
 */
public class ConnectionDebugUtil {
    
    public static void main(String[] args) {
        System.out.println("===== DEBUG DE CONEXÃO SUPABASE =====");
        
        // Verificar variáveis de ambiente
        String jdbcUrl = System.getenv("JDBC_DATABASE_URL");
        String jdbcUsername = System.getenv("JDBC_DATABASE_USERNAME");
        String jdbcPassword = System.getenv("JDBC_DATABASE_PASSWORD");
        
        System.out.println("\n--- Variáveis de Ambiente ---");
        System.out.println("JDBC_DATABASE_URL: " + (jdbcUrl != null ? jdbcUrl.substring(0, Math.min(jdbcUrl.length(), 20)) + "..." : "NÃO DEFINIDA"));
        System.out.println("JDBC_DATABASE_USERNAME: " + (jdbcUsername != null ? jdbcUsername : "NÃO DEFINIDA"));
        System.out.println("JDBC_DATABASE_PASSWORD: " + (jdbcPassword != null ? "********" : "NÃO DEFINIDA"));
        
        // Verificar driver PostgreSQL
        System.out.println("\n--- Driver PostgreSQL ---");
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Driver PostgreSQL carregado com sucesso!");
        } catch (ClassNotFoundException e) {
            System.out.println("ERRO: Driver PostgreSQL não encontrado!");
            e.printStackTrace();
        }
        
        // Verificar formato da URL
        System.out.println("\n--- Análise da URL ---");
        if (jdbcUrl != null) {
            if (jdbcUrl.startsWith("jdbc:postgresql://")) {
                System.out.println("✅ Formato da URL está correto (jdbc:postgresql://)");
            } else if (jdbcUrl.startsWith("postgresql://")) {
                System.out.println("❌ ERRO: URL começa com 'postgresql://' em vez de 'jdbc:postgresql://'");
                System.out.println("   Corrija para: jdbc:postgresql://" + jdbcUrl.substring(13));
            } else {
                System.out.println("❌ ERRO: Formato da URL não reconhecido");
            }
            
            // Verificar parâmetros SSL
            if (jdbcUrl.contains("sslmode=")) {
                System.out.println("✅ Parâmetro sslmode encontrado na URL");
            } else {
                System.out.println("⚠️ ATENÇÃO: Parâmetro sslmode não encontrado na URL");
                System.out.println("   Considere adicionar '?sslmode=require' ao final da URL");
            }
        }
        
        // Instruções para resolução de problemas
        System.out.println("\n--- Instruções para resolução de problemas ---");
        System.out.println("1. Verifique se a URL está no formato correto: jdbc:postgresql://servidor:porta/banco");
        System.out.println("2. Verifique se o Supabase permite conexões do Render (pode exigir IP estático)");
        System.out.println("3. Tente adicionar '?sslmode=require' ao final da URL se não estiver presente");
        System.out.println("4. Certifique-se de que as credenciais (usuário/senha) estão corretas");
        
        System.out.println("\n=== URL FORMATADA CORRETAMENTE (exemplo) ===");
        if (jdbcUrl != null && jdbcUrl.startsWith("postgresql://")) {
            String correctedUrl = "jdbc:postgresql://" + jdbcUrl.substring(13);
            if (!correctedUrl.contains("?")) {
                correctedUrl += "?sslmode=require";
            } else if (!correctedUrl.contains("sslmode=")) {
                correctedUrl += "&sslmode=require";
            }
            System.out.println(correctedUrl);
        } else {
            System.out.println("jdbc:postgresql://db.xxx.supabase.co:5432/postgres?sslmode=require");
        }
    }
}
