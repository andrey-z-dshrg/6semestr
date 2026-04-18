package com.example.demo.model;

import jakarta.persistence.*;
import com.example.demo.entity.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "license_history")
// Сущность или модель предметной области. Она описывает, какие данные приложение хранит и как эти данные связаны между собой.
public class LicenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private License license;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private User user;

    @Column(nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private String status;

    @Column(name = "change_date", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private LocalDateTime changeDate;

    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private String description;

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public Long getId() { return id; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public License getLicense() { return license; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public User getUser() { return user; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getStatus() { return status; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public LocalDateTime getChangeDate() { return changeDate; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getDescription() { return description; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setLicense(License license) { this.license = license; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setUser(User user) { this.user = user; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setStatus(String status) { this.status = status; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setDescription(String description) { this.description = description; }
}
