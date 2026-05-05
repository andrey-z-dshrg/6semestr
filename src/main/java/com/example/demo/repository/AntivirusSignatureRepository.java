package com.example.demo.repository;

import com.example.demo.model.AntivirusSignature;
import com.example.demo.model.AntivirusSignatureStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Репозиторий основной таблицы signatures.
// Через него сервис получает актуальные записи для JSON API и binary API.
public interface AntivirusSignatureRepository extends JpaRepository<AntivirusSignature, UUID> {

    // Полная выгрузка должна содержать только рабочие записи.
    List<AntivirusSignature> findAllByStatusOrderByUpdatedAtAscIdAsc(AntivirusSignatureStatus status);

    // Инкремент строится по всем записям, которые изменились позже заданного момента.
    // Сюда попадают и ACTUAL, и DELETED, если их updatedAt больше since.
    List<AntivirusSignature> findAllByUpdatedAtAfterOrderByUpdatedAtAscIdAsc(LocalDateTime since);
}
