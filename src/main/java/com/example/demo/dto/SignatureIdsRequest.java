package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// DTO для операции "получить сигнатуры по списку id".
// Клиент отправляет сюда массив идентификаторов, а сервис возвращает только соответствующие записи.
public class SignatureIdsRequest {

    @NotEmpty
    // Список id сигнатур, которые клиент хочет получить.
    private List<@NotNull Long> ids;

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public List<Long> getIds() {
        return ids;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
