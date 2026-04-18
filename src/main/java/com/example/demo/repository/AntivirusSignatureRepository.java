package com.example.demo.repository;

import com.example.demo.model.AntivirusSignature;
import com.example.demo.model.AntivirusSignatureStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

// Репозиторий основной таблицы сигнатур.
// Через него сервис получает текущее состояние записей и строит полную/инкрементальную выгрузку.
public interface AntivirusSignatureRepository extends JpaRepository<AntivirusSignature, Long> {

    // Выбирает все записи, кроме заданного статуса.
    // В проекте используется для полной выгрузки без DELETED.
    List<AntivirusSignature> findAllByStatusNotOrderByIdAsc(AntivirusSignatureStatus status);

    // Выбирает все записи, изменённые после указанного времени.
    // В проекте это основа для инкрементальной выгрузки.
    List<AntivirusSignature> findAllByUpdatedAtAfterOrderByUpdatedAtAscIdAsc(LocalDateTime since);
}
