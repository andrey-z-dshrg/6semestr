package com.example.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "antivirus_signatures")
// Главная сущность модуля сигнатур.
// Именно эта таблица хранит текущее состояние записи, которое видят full export, increment и verify.
public class AntivirusSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Первичный ключ записи в основной таблице.
    private Long id;

    @Column(nullable = false, length = 150)
    // Короткое имя сигнатуры.
    private String signatureName;

    @Column(nullable = false, length = 150)
    // Название угрозы.
    private String malwareName;

    @Column(nullable = false, columnDefinition = "TEXT")
    // Основное содержимое сигнатуры.
    private String signatureBody;

    @Column(columnDefinition = "TEXT")
    // Описание записи для человека.
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    // Статус записи.
    // ACTIVE означает рабочую сигнатуру, DELETED - логически удалённую.
    private AntivirusSignatureStatus status = AntivirusSignatureStatus.ACTIVE;

    @Column(nullable = false, columnDefinition = "TEXT")
    // Цифровая подпись текущего состояния записи.
    private String digitalSignature;

    @Column(nullable = false)
    // Когда запись была создана впервые.
    private LocalDateTime createdAt;

    @Column(nullable = false)
    // Когда запись менялась в последний раз.
    private LocalDateTime updatedAt;

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public Long getId() {
        return id;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setId(Long id) {
        this.id = id;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getSignatureName() {
        return signatureName;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getMalwareName() {
        return malwareName;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setMalwareName(String malwareName) {
        this.malwareName = malwareName;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getSignatureBody() {
        return signatureBody;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setSignatureBody(String signatureBody) {
        this.signatureBody = signatureBody;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getDescription() {
        return description;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setDescription(String description) {
        this.description = description;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public AntivirusSignatureStatus getStatus() {
        return status;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setStatus(AntivirusSignatureStatus status) {
        this.status = status;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public String getDigitalSignature() {
        return digitalSignature;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setDigitalSignature(String digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
