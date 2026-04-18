package com.example.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "antivirus_signature_audit")
// Сущность таблицы audit.
// Каждая строка здесь описывает одно действие над сигнатурой, но не хранит полный снимок её данных.
public class AntivirusSignatureAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Первичный ключ строки аудита.
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signature_id", nullable = false)
    // Ссылка на сигнатуру, над которой произошло действие.
    private AntivirusSignature signature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    // Тип действия: CREATE, UPDATE или DELETE.
    private AntivirusSignatureAuditAction action;

    @Column(nullable = false, length = 100)
    // Пользователь или система, выполнившие действие.
    private String actor;

    @Column(length = 500)
    // Короткое пояснение о событии.
    private String details;

    @Column(nullable = false)
    // Когда событие было записано в audit.
    private LocalDateTime actionAt;

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public Long getId() {
        return id;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setId(Long id) {
        this.id = id;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public AntivirusSignature getSignature() {
        return signature;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setSignature(AntivirusSignature signature) {
        this.signature = signature;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public AntivirusSignatureAuditAction getAction() {
        return action;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setAction(AntivirusSignatureAuditAction action) {
        this.action = action;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getActor() {
        return actor;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setActor(String actor) {
        this.actor = actor;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getDetails() {
        return details;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setDetails(String details) {
        this.details = details;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public LocalDateTime getActionAt() {
        return actionAt;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setActionAt(LocalDateTime actionAt) {
        this.actionAt = actionAt;
    }
}
