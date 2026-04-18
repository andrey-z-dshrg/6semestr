package com.example.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "antivirus_signature_history")
// Сущность таблицы history.
// В отличие от audit, здесь хранится не факт действия, а слепок старого состояния записи.
public class AntivirusSignatureHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Первичный ключ строки history.
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signature_id", nullable = false)
    // Сигнатура, чья старая версия была сохранена.
    private AntivirusSignature signature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    // Причина попадания строки в history: UPDATE или DELETE.
    private AntivirusSignatureHistoryAction action;

    @Column(nullable = false, length = 150)
    // Старое имя сигнатуры.
    private String signatureName;

    @Column(nullable = false, length = 150)
    // Старое название угрозы.
    private String malwareName;

    @Column(nullable = false, columnDefinition = "TEXT")
    // Старое тело сигнатуры.
    private String signatureBody;

    @Column(columnDefinition = "TEXT")
    // Старое описание.
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    // Статус записи на момент сохранения снимка.
    private AntivirusSignatureStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    // Подпись той версии записи, которая ушла в history.
    private String digitalSignature;

    @Column(nullable = false)
    // Исходное время создания основной записи.
    private LocalDateTime originalCreatedAt;

    @Column(nullable = false)
    // Время последнего изменения той версии, которая сохранялась.
    private LocalDateTime originalUpdatedAt;

    @Column(nullable = false)
    // Момент, когда строка была добавлена в таблицу history.
    private LocalDateTime historyCreatedAt;

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
    public AntivirusSignatureHistoryAction getAction() {
        return action;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setAction(AntivirusSignatureHistoryAction action) {
        this.action = action;
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
    public LocalDateTime getOriginalCreatedAt() {
        return originalCreatedAt;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setOriginalCreatedAt(LocalDateTime originalCreatedAt) {
        this.originalCreatedAt = originalCreatedAt;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public LocalDateTime getOriginalUpdatedAt() {
        return originalUpdatedAt;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setOriginalUpdatedAt(LocalDateTime originalUpdatedAt) {
        this.originalUpdatedAt = originalUpdatedAt;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public LocalDateTime getHistoryCreatedAt() {
        return historyCreatedAt;
    }

    // Геттер или сеттер сущности. Через такие методы другие слои читают поля объекта или меняют их перед сохранением.
    public void setHistoryCreatedAt(LocalDateTime historyCreatedAt) {
        this.historyCreatedAt = historyCreatedAt;
    }
}
