package com.example.demo.dto;

import com.example.demo.model.AntivirusSignatureHistoryAction;
import com.example.demo.model.AntivirusSignatureStatus;

import java.time.LocalDateTime;

// DTO ответа для таблицы history.
// Он показывает не текущее состояние сигнатуры, а её прошлую версию, сохранённую перед update или delete.
public record AntivirusSignatureHistoryResponse(
        // Id самой строки history.
        Long id,
        // Id сигнатуры, к которой относится эта историческая запись.
        Long signatureId,
        // Причина попадания в history: UPDATE или DELETE.
        AntivirusSignatureHistoryAction action,
        // Старое имя сигнатуры на момент сохранения снимка.
        String signatureName,
        // Старое название угрозы.
        String malwareName,
        // Старое тело сигнатуры.
        String signatureBody,
        // Старое описание.
        String description,
        // Статус записи в тот момент, когда снимок ушёл в history.
        AntivirusSignatureStatus status,
        // Подпись именно той версии, которая попала в history.
        String digitalSignature,
        // Исходное время создания основной записи.
        LocalDateTime originalCreatedAt,
        // Время последнего изменения той версии, которая сохранялась в history.
        LocalDateTime originalUpdatedAt,
        // Момент, когда строка была записана в таблицу history.
        LocalDateTime historyCreatedAt
) {
}
