package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// По структуре update почти совпадает с create.
// Разница в том, что эти данные заменят существующее состояние записи, а старое состояние перед этим должно уйти в history.
public class AntivirusSignatureUpdateRequest {

    @NotBlank
    @Size(max = 255)
    private String threatName;

    @NotBlank
    @Pattern(regexp = "^[0-9A-Fa-f]+$", message = "firstBytesHex must contain only hex characters")
    @Size(max = 512)
    private String firstBytesHex;

    @NotBlank
    @Pattern(regexp = "^[0-9A-Fa-f]+$", message = "remainderHashHex must contain only hex characters")
    @Size(max = 512)
    private String remainderHashHex;

    @NotNull
    @PositiveOrZero
    private Long remainderLength;

    @NotBlank
    @Size(max = 100)
    private String fileType;

    @NotNull
    @PositiveOrZero
    private Long offsetStart;

    @NotNull
    @PositiveOrZero
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
