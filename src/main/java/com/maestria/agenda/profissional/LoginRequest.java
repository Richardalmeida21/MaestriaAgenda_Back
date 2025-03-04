package com.maestria.agenda.profissional;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public class LoginRequest {

    @NotBlank(message = "Username não pode ser vazio")
    private String username;

    @NotBlank(message = "Senha não pode ser vazia")
    private String senha;


    public LoginRequest() {
    }

    public LoginRequest(String username, String senha) {
        this.username = username;
        this.senha = senha;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LoginRequest username(String username) {
        setUsername(username);
        return this;
    }

    public LoginRequest senha(String senha) {
        setSenha(senha);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof LoginRequest)) {
            return false;
        }
        LoginRequest loginRequest = (LoginRequest) o;
        return Objects.equals(username, loginRequest.username) && Objects.equals(senha, loginRequest.senha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, senha);
    }

    @Override
    public String toString() {
        return "{" +
            " username='" + getUsername() + "'" +
            ", senha='" + getSenha() + "'" +
            "}";
    }
    
}
