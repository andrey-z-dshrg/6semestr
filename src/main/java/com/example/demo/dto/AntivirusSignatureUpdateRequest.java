package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO запроса на обновление сигнатуры.
// По структуре он похож на create-запрос, но по смыслу используется уже для изменения существующей записи.
public class AntivirusSignatureUpdateRequest {

    @NotBlank
    @Size(max = 150)
    // Новое имя сигнатуры, которое после update должно заменить старое.
    private String signatureName;

    @NotBlank
    @Size(max = 150)
    // Новое значение поля malwareName для обновляемой записи.
    private String malwareName;

    @NotBlank
    // Новое тело сигнатуры.
    // Если меняется это поле, подпись обязательно должна стать другой.
    private String signatureBody;

    // Новое текстовое описание.
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
