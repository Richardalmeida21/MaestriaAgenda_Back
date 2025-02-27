package com.maestria.agenda.profissional;

public class RegistrationRequest {
    private String username;
    private String password;

    // Construtor padrão
    public RegistrationRequest() {}

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}