package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "signatures_audit")
// Аудит отвечает на другой вопрос, чем история.
// История хранит старые версии данных, а аудит хранит сам факт действия: кто, когда и что именно поменял.
public class AntivirusSignatureAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id", nullable = false, updatable = false)
    private Long auditId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signature_id", nullable = false)
    // Через эту ссылку можно получить сигнатуру, к которой относится событие аудита.
    private AntivirusSignature signature;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "fields_changed", nullable = false, columnDefinition = "TEXT")
    // Это JSON-строка с массивом полей, которые реально изменились.
    // Например: {"changed":["fileType","offsetEnd"]}.
    private String fieldsChanged;

    @Column(nullable = false, length = 500)
    // Короткая фраза, по которой можно быстро понять, что произошло: create, update или logical delete.
    private String description;

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public AntivirusSignature getSignature() {
        return signature;
    }

    public void setSignature(AntivirusSignature signature) {
        this.signature = signature;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getFieldsChanged() {
        return fieldsChanged;
    }

    public void setFieldsChanged(String fieldsChanged) {
        this.fieldsChanged = fieldsChanged;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
