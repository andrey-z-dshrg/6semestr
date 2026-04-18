package com.example.demo.dto;

// DTO-класс для обмена данными между клиентом, контроллером и сервисом. В нём нет сложной логики: он нужен, чтобы удобно переносить данные.
public class CreateLicenseRequest {
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private Long productId;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private Long typeId;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private Long ownerId;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private int deviceCount;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private String description;

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public Long getProductId() { return productId; }
    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public Long getTypeId() { return typeId; }
    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public Long getOwnerId() { return ownerId; }
    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public int getDeviceCount() { return deviceCount; }
    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getDescription() { return description; }
}
