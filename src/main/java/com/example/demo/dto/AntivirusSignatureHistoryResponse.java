package com.example.demo.dto;

import com.example.demo.model.AntivirusSignatureStatus;

import java.time.LocalDateTime;
import java.util.UUID;

// Этот ответ показывает не текущее состояние сигнатуры, а один снимок из её истории.
// Он нужен, чтобы доказать, что при update и delete старая версия реально сохраняется.
public record AntivirusSignatureHistoryResponse(
        Long historyId,
        UUID signatureId,
        LocalDateTime versionCreatedAt,
        String threatName,
        String firstBytesHex,
        String remainderHashHex,
        Long remainderLength,
        String fileType,
        Long offsetStart,
        Long offsetEnd,
        LocalDateTime updatedAt,
        AntivirusSignatureStatus status,
        String digitalSignatureBase64
) {
}
