package com.example.demo.signature;

import com.example.demo.dto.AntivirusSignatureCreateRequest;
import com.example.demo.dto.AntivirusSignatureResponse;
import com.example.demo.dto.AntivirusSignatureUpdateRequest;
import com.example.demo.model.AntivirusSignatureStatus;
import com.example.demo.repository.AntivirusSignatureAuditRepository;
import com.example.demo.repository.AntivirusSignatureHistoryRepository;
import com.example.demo.repository.AntivirusSignatureRepository;
import com.example.demo.service.AntivirusSignatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
// Этот тест имитирует практически всю защиту 4-й практической в одном сценарии.
// Он не проверяет отдельную строчку кода, а проходит по бизнес-цепочке целиком:
// create -> verify -> update -> history/audit -> delete -> increment/full export.
class AntivirusSignatureServiceIntegrationTest {

    @Autowired
    // Сам сервис, который и является главным объектом проверки в этом тесте.
    private AntivirusSignatureService antivirusSignatureService;

    @Autowired
    // Репозиторий основной таблицы используется, чтобы при необходимости проверить состояние базы напрямую.
    private AntivirusSignatureRepository signatureRepository;

    @Autowired
    // Репозиторий history нужен для очистки базы перед каждым сценарием.
    private AntivirusSignatureHistoryRepository historyRepository;

    @Autowired
    // Репозиторий audit тоже очищается перед стартом сценария.
    private AntivirusSignatureAuditRepository auditRepository;

    @BeforeEach
    void setUp() {
        auditRepository.deleteAll();
        historyRepository.deleteAll();
        signatureRepository.deleteAll();
    }

    @Test
    void shouldSupportAllRequiredOperationsAndRules() throws InterruptedException {
        AntivirusSignatureCreateRequest createRequest = new AntivirusSignatureCreateRequest();
        createRequest.setSignatureName("Trojan.Win32.Agent");
        createRequest.setMalwareName("Agent");
        createRequest.setSignatureBody("4D5A90000300000004000000FFFF");
        createRequest.setDescription("Initial signature");

        AntivirusSignatureResponse created = antivirusSignatureService.create(createRequest, "admin1");
        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(AntivirusSignatureStatus.ACTIVE);
        assertThat(created.digitalSignature()).isNotBlank();
        assertThat(antivirusSignatureService.verify(created.id()).valid()).isTrue();

        assertThat(antivirusSignatureService.getFullExport()).hasSize(1);
        assertThat(antivirusSignatureService.getByIds(List.of(created.id()))).hasSize(1);

        LocalDateTime sinceBeforeUpdate = created.updatedAt();
        Thread.sleep(1100);

        AntivirusSignatureUpdateRequest updateRequest = new AntivirusSignatureUpdateRequest();
        updateRequest.setSignatureName("Trojan.Win32.Agent.Updated");
        updateRequest.setMalwareName("Agent");
        updateRequest.setSignatureBody("4D5A90000300000004000000AAAABBBB");
        updateRequest.setDescription("Updated signature");

        AntivirusSignatureResponse updated = antivirusSignatureService.update(created.id(), updateRequest, "admin1");
        assertThat(updated.digitalSignature()).isNotEqualTo(created.digitalSignature());
        assertThat(updated.updatedAt()).isAfter(created.updatedAt());
        assertThat(antivirusSignatureService.verify(updated.id()).valid()).isTrue();

        List<AntivirusSignatureResponse> incrementAfterUpdate = antivirusSignatureService.getIncrement(sinceBeforeUpdate);
        assertThat(incrementAfterUpdate).extracting(AntivirusSignatureResponse::id).contains(updated.id());

        assertThat(antivirusSignatureService.getHistory(updated.id())).hasSize(1);
        assertThat(antivirusSignatureService.getAudit(updated.id())).hasSize(2);

        LocalDateTime sinceBeforeDelete = updated.updatedAt();
        Thread.sleep(1100);

        AntivirusSignatureResponse deleted = antivirusSignatureService.delete(updated.id(), "admin1");
        assertThat(deleted.status()).isEqualTo(AntivirusSignatureStatus.DELETED);
        assertThat(signatureRepository.findById(deleted.id())).isPresent();
        assertThat(antivirusSignatureService.verify(deleted.id()).valid()).isTrue();

        assertThat(antivirusSignatureService.getFullExport()).isEmpty();

        List<AntivirusSignatureResponse> incrementAfterDelete = antivirusSignatureService.getIncrement(sinceBeforeDelete);
        assertThat(incrementAfterDelete).hasSize(1);
        assertThat(incrementAfterDelete.getFirst().status()).isEqualTo(AntivirusSignatureStatus.DELETED);

        assertThat(antivirusSignatureService.getHistory(deleted.id())).hasSize(2);
        assertThat(antivirusSignatureService.getAudit(deleted.id())).hasSize(3);

        assertThat(historyRepository.findAllBySignature_IdOrderByHistoryCreatedAtDescIdDesc(deleted.id())).hasSize(2);
        assertThat(auditRepository.findAllBySignature_IdOrderByActionAtDescIdDesc(deleted.id())).hasSize(3);
    }
}
