package com.example.demo.dto;

import com.example.demo.model.AntivirusSignatureStatus;

import java.time.LocalDateTime;
import java.util.UUID;

// Это основной DTO ответа по сигнатуре.
// Его получают и обычные JSON-запросы чтения, и ответы после create/update/delete.
public record AntivirusSignatureResponse(
        UUID id,
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
