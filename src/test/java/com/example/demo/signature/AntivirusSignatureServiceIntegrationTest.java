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
// Этот тест проходит по всей основной бизнес-цепочке модуля сигнатур.
// Он нужен, чтобы убедиться, что после переделки под ЗИОВПО и binary API старая логика 4 задания тоже осталась рабочей.
class AntivirusSignatureServiceIntegrationTest {

    @Autowired
    private AntivirusSignatureService antivirusSignatureService;

    @Autowired
    private AntivirusSignatureRepository signatureRepository;

    @Autowired
    private AntivirusSignatureHistoryRepository historyRepository;

    @Autowired
    private AntivirusSignatureAuditRepository auditRepository;

    @BeforeEach
    void setUp() {
        auditRepository.deleteAll();
        historyRepository.deleteAll();
        signatureRepository.deleteAll();
    }

    @Test
    void shouldSupportRequiredJsonOperations() throws InterruptedException {
        AntivirusSignatureCreateRequest createRequest = new AntivirusSignatureCreateRequest();
        createRequest.setThreatName("Trojan.Win32.Agent");
        createRequest.setFirstBytesHex("4D5A90000300000004000000");
        createRequest.setRemainderHashHex("5F4DCC3B5AA765D61D8327DEB882CF99");
        createRequest.setRemainderLength(4096L);
        createRequest.setFileType("exe");
        createRequest.setOffsetStart(128L);
        createRequest.setOffsetEnd(256L);

        AntivirusSignatureResponse created = antivirusSignatureService.create(createRequest, "admin1");
        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(AntivirusSignatureStatus.ACTUAL);
        assertThat(created.digitalSignatureBase64()).isNotBlank();
        assertThat(antivirusSignatureService.verify(created.id()).valid()).isTrue();

        assertThat(antivirusSignatureService.getFullExport()).hasSize(1);
        assertThat(antivirusSignatureService.getByIds(List.of(created.id()))).hasSize(1);

        LocalDateTime sinceBeforeUpdate = created.updatedAt();
        Thread.sleep(1100);

        AntivirusSignatureUpdateRequest updateRequest = new AntivirusSignatureUpdateRequest();
        updateRequest.setThreatName("Trojan.Win32.Agent.V2");
        updateRequest.setFirstBytesHex("4D5A90000300000004000000AA");
        updateRequest.setRemainderHashHex("A94A8FE5CCB19BA61C4C0873D391E987");
        updateRequest.setRemainderLength(8192L);
        updateRequest.setFileType("dll");
        updateRequest.setOffsetStart(512L);
        updateRequest.setOffsetEnd(768L);

        AntivirusSignatureResponse updated = antivirusSignatureService.update(created.id(), updateRequest, "admin1");
        assertThat(updated.updatedAt()).isAfter(created.updatedAt());
        assertThat(updated.threatName()).isEqualTo("Trojan.Win32.Agent.V2");
        assertThat(updated.digitalSignatureBase64()).isNotEqualTo(created.digitalSignatureBase64());
        assertThat(antivirusSignatureService.verify(updated.id()).valid()).isTrue();

        List<AntivirusSignatureResponse> incrementAfterUpdate = antivirusSignatureService.getIncrement(sinceBeforeUpdate);
        assertThat(incrementAfterUpdate).extracting(AntivirusSignatureResponse::id).contains(updated.id());

        assertThat(antivirusSignatureService.getHistory(updated.id())).hasSize(1);
        assertThat(antivirusSignatureService.getHistory(updated.id()).getFirst().threatName()).isEqualTo("Trojan.Win32.Agent");
        assertThat(antivirusSignatureService.getAudit(updated.id())).hasSize(2);
        assertThat(antivirusSignatureService.getAudit(updated.id()).getFirst().fieldsChanged())
                .contains("threatName", "firstBytesHex");

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
        assertThat(antivirusSignatureService.getAudit(deleted.id()).getFirst().description())
                .isEqualTo("Signature logically deleted");

        assertThat(historyRepository.findAllBySignature_IdOrderByVersionCreatedAtDescHistoryIdDesc(deleted.id())).hasSize(2);
        assertThat(auditRepository.findAllBySignature_IdOrderByChangedAtDescAuditIdDesc(deleted.id())).hasSize(3);
    }
}
