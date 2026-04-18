package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO запроса на создание сигнатуры.
// Именно такой объект Spring собирает из JSON-тела POST /api/signatures,
// а затем сервис использует его как источник новых данных для записи.
public class AntivirusSignatureCreateRequest {

    @NotBlank
    @Size(max = 150)
    // Человеко-понятное имя сигнатуры.
    // Обычно это короткое обозначение, по которому запись легко узнать в списке.
    private String signatureName;

    @NotBlank
    @Size(max = 150)
    // Название угрозы или семейства вредоносного ПО, которому соответствует сигнатура.
    private String malwareName;

    @NotBlank
    // Основное содержимое сигнатуры.
    // Это одно из ключевых полей, которое участвует в формировании подписи записи.
    private String signatureBody;

    // Дополнительное описание сигнатуры обычным текстом.
    // Поле не обязательно для алгоритма, но помогает понять смысл записи человеку.
    private String description;

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getSignatureName() {
        return signatureName;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getMalwareName() {
        return malwareName;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setMalwareName(String malwareName) {
        this.malwareName = malwareName;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getSignatureBody() {
        return signatureBody;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setSignatureBody(String signatureBody) {
        this.signatureBody = signatureBody;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getDescription() {
        return description;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setDescription(String description) {
        this.description = description;
    }
}
