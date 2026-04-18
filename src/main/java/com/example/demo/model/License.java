package com.example.demo.model;

import jakarta.persistence.*;
import com.example.demo.entity.User;
import java.time.LocalDate;

@Entity
@Table(name = "license")
// Сущность или модель предметной области. Она описывает, какие данные приложение хранит и как эти данные связаны между собой.
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private Long id;

    @Column(nullable = false, unique = true)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private LicenseType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private User user;

    @Column(name = "first_activation_date")
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private LocalDate firstActivationDate;

    @Column(name = "ending_date")
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private LocalDate endingDate;

    @Column(nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private boolean blocked = false;

    @Column(name = "device_count", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private int deviceCount;

    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private String description;

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public Long getId() { return id; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getCode() { return code; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public Product getProduct() { return product; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public LicenseType getType() { return type; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public User getOwner() { return owner; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public User getUser() { return user; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public LocalDate getFirstActivationDate() { return firstActivationDate; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public LocalDate getEndingDate() { return endingDate; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public boolean isBlocked() { return blocked; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public int getDeviceCount() { return deviceCount; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getDescription() { return description; }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setId(Long id) { this.id = id; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setCode(String code) { this.code = code; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setProduct(Product product) { this.product = product; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setType(LicenseType type) { this.type = type; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setOwner(User owner) { this.owner = owner; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setUser(User user) { this.user = user; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setFirstActivationDate(LocalDate d) { this.firstActivationDate = d; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setEndingDate(LocalDate d) { this.endingDate = d; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setDescription(String description) { this.description = description; }
}
