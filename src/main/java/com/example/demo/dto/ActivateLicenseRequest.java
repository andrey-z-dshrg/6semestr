package com.example.demo.dto;

// DTO-класс для обмена данными между клиентом, контроллером и сервисом. В нём нет сложной логики: он нужен, чтобы удобно переносить данные.
public class ActivateLicenseRequest {
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private String activationKey;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private String deviceMac;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private String deviceName;

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getActivationKey() { return activationKey; }
    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getDeviceMac() { return deviceMac; }
    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getDeviceName() { return deviceName; }
}
