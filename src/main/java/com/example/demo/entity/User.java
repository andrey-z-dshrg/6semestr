package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
// Сущность или модель предметной области. Она описывает, какие данные приложение хранит и как эти данные связаны между собой.
public class User implements UserDetails {

    // Константа класса. Такие значения выносят отдельно, чтобы их было легко найти и не дублировать по коду.
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private String username;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private String email;

    @Column(nullable = false)
    private String role = "USER";

    @Column(columnDefinition = "boolean default true")
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private boolean enabled = true;

    public User() {}

    public User(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role != null ? role : "USER";
        this.enabled = true;
    }

    @Override
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getPassword() {
        return password;
    }

    @Override
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getUsername() {
        return username;
    }

    @Override
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public boolean isEnabled() {
        return enabled;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public Long getId() {
        return id;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setId(Long id) {
        this.id = id;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setUsername(String username) {
        this.username = username;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setPassword(String password) {
        this.password = password;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getEmail() {
        return email;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setEmail(String email) {
        this.email = email;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getRole() {
        return role;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setRole(String role) {
        this.role = role != null ? role : "USER";
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
