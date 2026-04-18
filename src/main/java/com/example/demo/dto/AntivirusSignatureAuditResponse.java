package com.example.demo.dto;

import com.example.demo.model.AntivirusSignatureAuditAction;

import java.time.LocalDateTime;

// DTO ответа для таблицы audit.
// Этот объект не хранит старые данные сигнатуры, а описывает сам факт действия над ней.
public record AntivirusSignatureAuditResponse(
        // Id строки аудита.
        Long id,
        // Id сигнатуры, к которой относится событие.
        Long signatureId,
        // Тип действия: CREATE, UPDATE или DELETE.
        AntivirusSignatureAuditAction action,
        // Кто выполнил действие.
        String actor,
        // Короткое служебное пояснение о событии.
        String details,
        // Время записи события в аудит.
        LocalDateTime actionAt
) {
}
