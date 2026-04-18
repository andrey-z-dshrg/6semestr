package com.example.demo.dto;

// DTO-класс для обмена данными между клиентом, контроллером и сервисом. В нём нет сложной логики: он нужен, чтобы удобно переносить данные.
public class RenewLicenseRequest {
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private String activationKey;

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getActivationKey() { return activationKey; }
}
