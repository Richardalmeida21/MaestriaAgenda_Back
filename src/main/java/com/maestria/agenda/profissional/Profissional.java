package com.maestria.agenda.profissional;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name = "Profissional")
public class Profissional {

    // Definindo os tipos de papéis (role) como um Enum
    public enum Role {
        ADMIN,
        PROFISSIONAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Nome não pode ser vazio")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "Login não pode ser vazio")
    @Size(min = 5, message = "Login deve ter pelo menos 5 caracteres")
    @Column(nullable = false, unique = true)
    private String login;

    @NotBlank(message = "Senha não pode ser vazia")
    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING) // Usando Enum para garantir valores controlados para role
    @Column(nullable = false)
    private Role role; // Alterado para o tipo Role

    // Construtor padrão
    public Profissional() {}

    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Métodos equals e hashCode baseados no 'id' e 'login' para comparações eficientes
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profissional that = (Profissional) o;
        return id == that.id && Objects.equals(login, that.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login);
    }
}
