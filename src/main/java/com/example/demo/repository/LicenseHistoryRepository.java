package com.example.demo.repository;

import com.example.demo.model.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

// Репозиторий Spring Data. Через него сервисы читают и сохраняют данные, не прописывая SQL вручную для каждой операции.
public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {}
