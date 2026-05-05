package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

// Этот ответ нужен для показа журнала действий над сигнатурой.
// По нему видно, кто и когда менял запись, какие поля были затронуты и как сервис описал событие.
public record AntivirusSignatureAuditResponse(
        Long auditId,
        UUID signatureId,
        String changedBy,
        LocalDateTime changedAt,
        String fieldsChanged,
        String description
) {
}
