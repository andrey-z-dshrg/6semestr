package com.example.demo.dto;

import com.example.demo.model.AntivirusSignatureStatus;

import java.time.LocalDateTime;

// Основной DTO ответа по сигнатуре.
// Такой объект клиент получает почти во всех операциях модуля: full export, increment, by-ids, create, update и delete.
public record AntivirusSignatureResponse(
        // Идентификатор записи в основной таблице сигнатур.
        Long id,
        // Короткое имя сигнатуры.
        String signatureName,
        // Название угрозы, с которой связана сигнатура.
        String malwareName,
        // Основное содержимое сигнатуры.
        String signatureBody,
        // Дополнительное пояснение к записи.
        String description,
        // Текущий статус: ACTIVE или DELETED.
        AntivirusSignatureStatus status,
        // Сохранённая цифровая подпись текущего состояния записи.
        String digitalSignature,
        // Время создания записи.
        LocalDateTime createdAt,
        // Время последнего изменения.
        LocalDateTime updatedAt
) {
}
