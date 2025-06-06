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
    @Size(min = 4, message = "Login deve ter pelo menos 4 caracteres")
    @Column(nullable = false, unique = true)
    private String login;

    @NotBlank(message = "Senha não pode ser vazia")
    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING) // Usando Enum para garantir valores controlados para role
    @Column(nullable = false)
    private Role role; // Alterado para o tipo Role

    // Removido o campo comissaoPercentual

    // Construtor padrão
    public Profissional() {
        // Role padrão para o profissional, caso não seja passado no cadastro
        this.role = Role.PROFISSIONAL;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Profissional other = (Profissional) obj;
        if (id != other.id)
            return false;
        if (nome == null) {
            if (other.nome != null)
                return false;
        } else if (!nome.equals(other.nome))
            return false;
        if (login == null) {
            if (other.login != null)
                return false;
        } else if (!login.equals(other.login))
            return false;
        if (senha == null) {
            if (other.senha != null)
                return false;
        } else if (!senha.equals(other.senha))
            return false;
        if (role != other.role)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((nome == null) ? 0 : nome.hashCode());
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + ((senha == null) ? 0 : senha.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        return result;
    }
}