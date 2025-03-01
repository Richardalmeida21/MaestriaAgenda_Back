package com.maestria.agenda.profissional;

public class RegistrationRequest {
    private String username;
    private String senha;
    private String nome;

    // Construtor padrão
    public RegistrationRequest() {}

    // Construtor com argumentos
    public RegistrationRequest(final String username, final String senha, final String nome) {
        this.username = username;
        this.senha = senha;
        this.nome = nome;
    }

    // Getters e Setters
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
