package com.example.demo.dto;

import com.example.demo.model.AntivirusSignatureStatus;

// Это не DTO для клиента, а специальный набор полей для модуля подписи.
// Сюда входят только те данные, которые по методичке действительно должны участвовать в подписи записи.
public record AntivirusSignaturePayload(
        String threatName,
        String firstBytesHex,
        String remainderHashHex,
        Long remainderLength,
        String fileType,
        Long offsetStart,
        Long offsetEnd,
        AntivirusSignatureStatus status
) {
}
