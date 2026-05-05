package com.example.demo.repository;

import com.example.demo.model.AntivirusSignatureAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Репозиторий журнала аудита.
// Через него можно поднять всю цепочку create/update/delete по конкретной сигнатуре.
public interface AntivirusSignatureAuditRepository extends JpaRepository<AntivirusSignatureAudit, Long> {

    List<AntivirusSignatureAudit> findAllBySignature_IdOrderByChangedAtDescAuditIdDesc(UUID signatureId);
}
