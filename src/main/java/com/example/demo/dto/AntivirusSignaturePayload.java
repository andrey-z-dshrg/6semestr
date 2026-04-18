package com.example.demo.dto;

import com.example.demo.model.AntivirusSignatureStatus;

import java.time.LocalDateTime;

// Специальный DTO для подписи.
// В отличие от Response, этот объект не нужен клиенту напрямую: он существует для того,
// чтобы модуль подписи получал строго определённый набор полей сигнатуры.
public record AntivirusSignaturePayload(
        // Id записи.
        Long id,
        // Имя сигнатуры.
        String signatureName,
        // Название угрозы.
        String malwareName,
        // Тело сигнатуры.
        String signatureBody,
        // Описание.
        String description,
        // Статус записи, который тоже влияет на подпись.
        AntivirusSignatureStatus status,
        // Время создания записи.
        LocalDateTime createdAt,
        // Время последнего изменения записи.
        LocalDateTime updatedAt
) {
}
