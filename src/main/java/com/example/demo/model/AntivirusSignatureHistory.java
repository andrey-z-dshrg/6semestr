package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "signatures_history")
// Эта таблица хранит прошлые версии записей.
// Сюда мы складываем полный снимок сигнатуры до update и до logical delete, чтобы потом можно было показать историю изменений.
public class AntivirusSignatureHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id", nullable = false, updatable = false)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signature_id", nullable = false)
    // Это ссылка на ту самую сигнатуру, чья старая версия была сохранена в историю.
    private AntivirusSignature signature;

    @Column(name = "version_created_at", nullable = false)
    // Время, когда именно эта историческая версия была записана в таблицу history.
    private LocalDateTime versionCreatedAt;

    @Column(name = "threat_name", nullable = false, length = 255)
    private String threatName;

    @Column(name = "first_bytes_hex", nullable = false, length = 512)
    private String firstBytesHex;

    @Column(name = "remainder_hash_hex", nullable = false, length = 512)
    private String remainderHashHex;

    @Column(name = "remainder_length", nullable = false)
    private Long remainderLength;

    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @Column(name = "offset_start", nullable = false)
    private Long offsetStart;

    @Column(name = "offset_end", nullable = false)
    private Long offsetEnd;

    @Column(name = "updated_at", nullable = false)
    // Значение updatedAt той версии, которая ушла в history.
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AntivirusSignatureStatus status;

    @Column(name = "digital_signature_base64", nullable = false, length = 2048)
    // Подпись не новой записи, а именно старой версии, которую мы сохранили.
    private String digitalSignatureBase64;

    @Column(name = "source_bucket", length = 255)
    private String sourceBucket;

    @Column(name = "source_object_key", length = 512)
    private String sourceObjectKey;

    @Column(name = "source_original_filename", length = 255)
    private String sourceOriginalFilename;

    @Column(name = "source_content_type", length = 255)
    private String sourceContentType;

    @Column(name = "source_size_bytes")
    private Long sourceSizeBytes;

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public AntivirusSignature getSignature() {
        return signature;
    }

    public void setSignature(AntivirusSignature signature) {
        this.signature = signature;
    }

    public LocalDateTime getVersionCreatedAt() {
        return versionCreatedAt;
    }

    public void setVersionCreatedAt(LocalDateTime versionCreatedAt) {
        this.versionCreatedAt = versionCreatedAt;
    }

    public String getThreatName() {
        return threatName;
    }

    public void setThreatName(String threatName) {
        this.threatName = threatName;
    }

    public String getFirstBytesHex() {
        return firstBytesHex;
    }

    public void setFirstBytesHex(String firstBytesHex) {
        this.firstBytesHex = firstBytesHex;
    }

    public String getRemainderHashHex() {
        return remainderHashHex;
    }

    public void setRemainderHashHex(String remainderHashHex) {
        this.remainderHashHex = remainderHashHex;
    }

    public Long getRemainderLength() {
        return remainderLength;
    }

    public void setRemainderLength(Long remainderLength) {
        this.remainderLength = remainderLength;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getOffsetStart() {
        return offsetStart;
    }

    public void setOffsetStart(Long offsetStart) {
        this.offsetStart = offsetStart;
    }

    public Long getOffsetEnd() {
        return offsetEnd;
    }

    public void setOffsetEnd(Long offsetEnd) {
        this.offsetEnd = offsetEnd;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public AntivirusSignatureStatus getStatus() {
        return status;
    }

    public void setStatus(AntivirusSignatureStatus status) {
        this.status = status;
    }

    public String getDigitalSignatureBase64() {
        return digitalSignatureBase64;
    }

    public void setDigitalSignatureBase64(String digitalSignatureBase64) {
        this.digitalSignatureBase64 = digitalSignatureBase64;
    }

    public String getSourceBucket() {
        return sourceBucket;
    }

    public void setSourceBucket(String sourceBucket) {
        this.sourceBucket = sourceBucket;
    }

    public String getSourceObjectKey() {
        return sourceObjectKey;
    }

    public void setSourceObjectKey(String sourceObjectKey) {
        this.sourceObjectKey = sourceObjectKey;
    }

    public String getSourceOriginalFilename() {
        return sourceOriginalFilename;
    }

    public void setSourceOriginalFilename(String sourceOriginalFilename) {
        this.sourceOriginalFilename = sourceOriginalFilename;
    }

    public String getSourceContentType() {
        return sourceContentType;
    }

    public void setSourceContentType(String sourceContentType) {
        this.sourceContentType = sourceContentType;
    }

    public Long getSourceSizeBytes() {
        return sourceSizeBytes;
    }

    public void setSourceSizeBytes(Long sourceSizeBytes) {
        this.sourceSizeBytes = sourceSizeBytes;
    }
}
