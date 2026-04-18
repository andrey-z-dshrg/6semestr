package com.example.demo.dto;

// DTO-класс для обмена данными между клиентом, контроллером и сервисом. В нём нет сложной логики: он нужен, чтобы удобно переносить данные.
public class CheckLicenseRequest {
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private String deviceMac;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private Long productId;

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getDeviceMac() { return deviceMac; }
    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public Long getProductId() { return productId; }
}
