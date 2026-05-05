package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Это тело запроса на создание новой сигнатуры.
// Именно из этих полей сервис собирает новую запись в таблице signatures и потом считает для неё цифровую подпись.
public class AntivirusSignatureCreateRequest {

    @NotBlank
    @Size(max = 255)
    // Основное имя угрозы.
    private String threatName;

    @NotBlank
    @Pattern(regexp = "^[0-9A-Fa-f]+$", message = "firstBytesHex must contain only hex characters")
    @Size(max = 512)
    // Первые байты сигнатуры в hex-виде.
    private String firstBytesHex;

    @NotBlank
    @Pattern(regexp = "^[0-9A-Fa-f]+$", message = "remainderHashHex must contain only hex characters")
    @Size(max = 512)
    // Хэш оставшейся части сигнатуры, тоже в hex-виде.
    private String remainderHashHex;

    @NotNull
    @PositiveOrZero
    // Длина хвоста сигнатуры.
    private Long remainderLength;

    @NotBlank
    @Size(max = 100)
    // Тип файла, для которого подходит сигнатура.
    private String fileType;

    @NotNull
    @PositiveOrZero
    // Начало диапазона сигнатуры внутри файла.
    private Long offsetStart;

    @NotNull
    @PositiveOrZero
    // Конец диапазона сигнатуры внутри файла.
    private Long offsetEnd;

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
}
