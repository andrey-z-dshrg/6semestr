package com.example.demo.repository;

import com.example.demo.model.AntivirusSignatureHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Репозиторий истории прошлых версий сигнатуры.
// Сортировка от новых к старым помогает удобно показывать последние изменения на защите и в Postman.
public interface AntivirusSignatureHistoryRepository extends JpaRepository<AntivirusSignatureHistory, Long> {

    List<AntivirusSignatureHistory> findAllBySignature_IdOrderByVersionCreatedAtDescHistoryIdDesc(UUID signatureId);
}
