package com.maestria.agenda.profissional;

public class LoginRequest {
    private String username; // Alterado de "login" para "username"
    private String senha;

    // Construtor padrão
    public LoginRequest() {}

    // Construtor com argumentos
    public LoginRequest(String username, String senha) {
        this.username = username;
        this.senha = senha;
    }

    // Getters e setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}