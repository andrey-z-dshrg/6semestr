package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signatures")
// Это основная таблица сигнатур.
// В ней лежит только актуальное состояние каждой записи, с которым работают JSON API, binary API и проверка подписи.
public class AntivirusSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    // UUID нужен, чтобы идентификатор был глобально уникальным и совпадал с форматом из методички.
    private UUID id;

    @Column(name = "threat_name", nullable = false, length = 255)
    // Имя угрозы, которое человек видит как основное название сигнатуры.
    private String threatName;

    @Column(name = "first_bytes_hex", nullable = false, length = 512)
    // Первые байты сигнатуры в hex-виде.
    // В JSON и базе это строка, а в data.bin это поле будет записано уже как настоящий массив байтов.
    private String firstBytesHex;

    @Column(name = "remainder_hash_hex", nullable = false, length = 512)
    // Хэш оставшейся части сигнатуры тоже хранится как hex-строка.
    private String remainderHashHex;

    @Column(name = "remainder_length", nullable = false)
    // Сколько байтов занимает оставшаяся часть сигнатуры.
    private Long remainderLength;

    @Column(name = "file_type", nullable = false, length = 100)
    // Тип файла, для которого эта сигнатура предназначена.
    private String fileType;

    @Column(name = "offset_start", nullable = false)
    // Начало диапазона в файле, где лежит сигнатура.
    private Long offsetStart;

    @Column(name = "offset_end", nullable = false)
    // Конец диапазона в файле, где лежит сигнатура.
    private Long offsetEnd;

    @Column(name = "updated_at", nullable = false)
    // Время последнего изменения записи.
    // Именно по нему строится инкрементальная выгрузка.
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    // Статус ACTUAL означает обычную рабочую запись, DELETED означает логически удалённую.
    private AntivirusSignatureStatus status = AntivirusSignatureStatus.ACTUAL;

    @Column(name = "digital_signature_base64", nullable = false, length = 2048)
    // Подпись текущего состояния записи.
    // Она не вычисляется в binary API заново, а берётся из этого поля и кладётся в manifest.bin.
    private String digitalSignatureBase64;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
}
