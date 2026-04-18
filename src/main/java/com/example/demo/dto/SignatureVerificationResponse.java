package com.example.demo.dto;

// Минимальный DTO ответа для проверки подписи.
// В рамках задания клиента в первую очередь интересует один факт:
// запись целая или нет.
public record SignatureVerificationResponse(
        // true - подпись соответствует текущим данным, false - данные и подпись больше не совпадают.
        boolean valid
) {
}
