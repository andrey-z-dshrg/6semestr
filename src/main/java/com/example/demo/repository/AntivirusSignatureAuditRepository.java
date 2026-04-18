package com.example.demo.repository;

import com.example.demo.model.AntivirusSignatureAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Репозиторий таблицы audit.
// Нужен, чтобы получить все события по конкретной сигнатуре и показать, кто что делал.
public interface AntivirusSignatureAuditRepository extends JpaRepository<AntivirusSignatureAudit, Long> {

    // Возвращает все события аудита по сигнатуре, начиная с самого нового.
    List<AntivirusSignatureAudit> findAllBySignature_IdOrderByActionAtDescIdDesc(Long signatureId);
}
