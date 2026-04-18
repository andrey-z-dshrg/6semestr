package com.example.demo.model;

import jakarta.persistence.*;
import com.example.demo.entity.User;

@Entity
@Table(name = "device")
// Сущность или модель предметной области. Она описывает, какие данные приложение хранит и как эти данные связаны между собой.
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private Long id;

    @Column(nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private String name;

    @Column(name = "mac_address", nullable = false, unique = true)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private String macAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private User user;

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public Long getId() { return id; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getName() { return name; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getMacAddress() { return macAddress; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public User getUser() { return user; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setId(Long id) { this.id = id; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setName(String name) { this.name = name; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setUser(User user) { this.user = user; }
}
