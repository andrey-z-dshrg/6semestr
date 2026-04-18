package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "product")
// Сущность или модель предметной области. Она описывает, какие данные приложение хранит и как эти данные связаны между собой.
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private Long id;

    @Column(nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private String name;

    @Column(name = "is_blocked", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private boolean isBlocked = false;

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public Long getId() { return id; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getName() { return name; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public boolean isBlocked() { return isBlocked; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setId(Long id) { this.id = id; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setName(String name) { this.name = name; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setBlocked(boolean blocked) { isBlocked = blocked; }
}
