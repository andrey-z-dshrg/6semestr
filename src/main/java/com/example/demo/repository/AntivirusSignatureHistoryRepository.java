package com.example.demo.repository;

import com.example.demo.model.AntivirusSignatureHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Репозиторий таблицы history.
// Позволяет получать прошлые версии сигнатуры в правильном порядке: от новой записи history к более старой.
public interface AntivirusSignatureHistoryRepository extends JpaRepository<AntivirusSignatureHistory, Long> {

    // Возвращает все исторические записи по одной сигнатуре, начиная с самой новой.
    List<AntivirusSignatureHistory> findAllBySignature_IdOrderByHistoryCreatedAtDescIdDesc(Long signatureId);
}
