package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "device_license",
        uniqueConstraints = @UniqueConstraint(columnNames = {"license_id", "device_id"}))
// Сущность или модель предметной области. Она описывает, какие данные приложение хранит и как эти данные связаны между собой.
public class DeviceLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private License license;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private Device device;

    @Column(name = "activation_date", nullable = false)
    // Поле сущности. Оно либо попадёт в базу данных, либо описывает связь с другой сущностью.
    private LocalDate activationDate;

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public Long getId() { return id; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public License getLicense() { return license; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public Device getDevice() { return device; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public LocalDate getActivationDate() { return activationDate; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setLicense(License license) { this.license = license; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setDevice(Device device) { this.device = device; }
    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setActivationDate(LocalDate activationDate) { this.activationDate = activationDate; }
}
