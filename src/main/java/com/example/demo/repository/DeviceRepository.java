package com.example.demo.repository;

import com.example.demo.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Репозиторий Spring Data. Через него сервисы читают и сохраняют данные, не прописывая SQL вручную для каждой операции.
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByMacAddress(String macAddress);
}
