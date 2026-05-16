package com.example.demo.service;

import com.example.demo.dto.AntivirusSignatureAuditResponse;
import com.example.demo.dto.AntivirusSignatureCreateRequest;
import com.example.demo.dto.AntivirusSignatureHistoryResponse;
import com.example.demo.dto.AntivirusSignaturePayload;
import com.example.demo.dto.AntivirusSignatureResponse;
import com.example.demo.dto.AntivirusSignatureUpdateRequest;
import com.example.demo.dto.SignatureVerificationResponse;
import com.example.demo.model.AntivirusSignature;
import com.example.demo.model.AntivirusSignatureAudit;
import com.example.demo.model.AntivirusSignatureHistory;
import com.example.demo.model.AntivirusSignatureStatus;
import com.example.demo.repository.AntivirusSignatureAuditRepository;
import com.example.demo.repository.AntivirusSignatureHistoryRepository;
import com.example.demo.repository.AntivirusSignatureRepository;
import com.example.demo.signature.SigningService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
// Это центральный сервис модуля сигнатур.
// Здесь живут все правила 4 и 5 задания: create/update/delete, история, аудит, подпись записи и данные для binary API.
public class AntivirusSignatureService {

    private static final String SYSTEM_ACTOR = "system";

    private final AntivirusSignatureRepository signatureRepository;
    private final AntivirusSignatureHistoryRepository historyRepository;
    private final AntivirusSignatureAuditRepository auditRepository;
    private final SigningService signingService;
    private final ObjectMapper objectMapper;

    public AntivirusSignatureService(AntivirusSignatureRepository signatureRepository,
                                     AntivirusSignatureHistoryRepository historyRepository,
                                     AntivirusSignatureAuditRepository auditRepository,
                                     SigningService signingService,
                                     ObjectMapper objectMapper) {
        this.signatureRepository = signatureRepository;
        this.historyRepository = historyRepository;
        this.auditRepository = auditRepository;
        this.signingService = signingService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    // Полная JSON-выгрузка должна отдавать только актуальные записи.
    public List<AntivirusSignatureResponse> getFullExport() {
        return getFullExportEntities().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    // Инкремент отдаёт всё, что изменилось позже since.
    // Здесь логически удалённые записи тоже нужны, иначе клиент не узнает, что их пора убрать у себя.
    public List<AntivirusSignatureResponse> getIncrement(LocalDateTime since) {
        return getIncrementEntities(since).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    // Этот метод нужен для адресной дозагрузки сигнатур по UUID.
    public List<AntivirusSignatureResponse> getByIds(List<UUID> ids) {
        return getByIdsEntities(ids).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    // При создании запись сначала получает бизнес-поля, затем время и статус,
    // после чего для неё считается подпись и в аудит пишется событие CREATE.
    public AntivirusSignatureResponse create(AntivirusSignatureCreateRequest request, String actor) {
        validateRequest(request.getFirstBytesHex(), request.getRemainderHashHex(), request.getOffsetStart(), request.getOffsetEnd());

        AntivirusSignature signature = new AntivirusSignature();
        applyRequest(signature, request);
        signature.setUpdatedAt(now());
        signature.setStatus(AntivirusSignatureStatus.ACTUAL);
        signature.setDigitalSignatureBase64("PENDING");

        signature = signatureRepository.saveAndFlush(signature);
        signature.setDigitalSignatureBase64(signRecord(signature));
        signature = signatureRepository.save(signature);

        saveAudit(signature, actor, allBusinessFields(), "Signature created");
        return toResponse(signature);
    }

    @Transactional
    // Обновление должно сделать три вещи:
    // 1. сохранить старую версию в history;
    // 2. пересчитать подпись уже для новых данных;
    // 3. записать в audit, какие поля реально поменялись.
    public AntivirusSignatureResponse update(UUID id, AntivirusSignatureUpdateRequest request, String actor) {
        validateRequest(request.getFirstBytesHex(), request.getRemainderHashHex(), request.getOffsetStart(), request.getOffsetEnd());

        AntivirusSignature signature = getEntity(id);
        ensureNotDeleted(signature);
        saveHistory(signature);

        List<String> changedFields = detectChangedFields(signature, request);
        applyRequest(signature, request);
        signature.setUpdatedAt(now());
        signature.setDigitalSignatureBase64(signRecord(signature));
        changedFields.add("updatedAt");

        AntivirusSignature saved = signatureRepository.save(signature);
        saveAudit(saved, actor, changedFields, "Signature updated");
        return toResponse(saved);
    }

    @Transactional
    // Удаление в этом проекте логическое.
    // Мы не стираем строку из базы, а переводим её в статус DELETED, чтобы она осталась доступной для истории и инкремента.
    public AntivirusSignatureResponse delete(UUID id, String actor) {
        AntivirusSignature signature = getEntity(id);
        if (signature.getStatus() == AntivirusSignatureStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "signature already deleted");
        }

        saveHistory(signature);

        signature.setStatus(AntivirusSignatureStatus.DELETED);
        signature.setUpdatedAt(now());
        signature.setDigitalSignatureBase64(signRecord(signature));

        AntivirusSignature saved = signatureRepository.save(signature);
        saveAudit(saved, actor, List.of("status", "updatedAt"), "Signature logically deleted");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    // История показывает старые версии записи, а не факты действий.
    public List<AntivirusSignatureHistoryResponse> getHistory(UUID signatureId) {
        ensureExists(signatureId);
        return historyRepository.findAllBySignature_IdOrderByVersionCreatedAtDescHistoryIdDesc(signatureId).stream()
                .map(history -> new AntivirusSignatureHistoryResponse(
                        history.getHistoryId(),
                        history.getSignature().getId(),
                        history.getVersionCreatedAt(),
                        history.getThreatName(),
                        history.getFirstBytesHex(),
                        history.getRemainderHashHex(),
                        history.getRemainderLength(),
                        history.getFileType(),
                        history.getOffsetStart(),
                        history.getOffsetEnd(),
                        history.getUpdatedAt(),
                        history.getStatus(),
                        history.getDigitalSignatureBase64()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    // Аудит показывает, кто и когда поменял запись, а также какие поля при этом затронул.
    public List<AntivirusSignatureAuditResponse> getAudit(UUID signatureId) {
        ensureExists(signatureId);
        return auditRepository.findAllBySignature_IdOrderByChangedAtDescAuditIdDesc(signatureId).stream()
                .map(audit -> new AntivirusSignatureAuditResponse(
                        audit.getAuditId(),
                        audit.getSignature().getId(),
                        audit.getChangedBy(),
                        audit.getChangedAt(),
                        audit.getFieldsChanged(),
                        audit.getDescription()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    // Проверка целостности строит payload из текущих полей записи и сравнивает его с подписью, хранящейся в базе.
    public SignatureVerificationResponse verify(UUID id) {
        AntivirusSignature signature = getEntity(id);
        return new SignatureVerificationResponse(
                signingService.verify(toPayload(signature), signature.getDigitalSignatureBase64())
        );
    }

    @Transactional(readOnly = true)
    // Эти методы нужны binary API.
    // Они отдают сами сущности, чтобы следующий слой мог собрать из них manifest.bin и data.bin.
    public List<AntivirusSignature> getFullExportEntities() {
        return signatureRepository.findAllByStatusOrderByUpdatedAtAscIdAsc(AntivirusSignatureStatus.ACTUAL);
    }

    @Transactional(readOnly = true)
    public List<AntivirusSignature> getIncrementEntities(LocalDateTime since) {
        return signatureRepository.findAllByUpdatedAtAfterOrderByUpdatedAtAscIdAsc(since);
    }

    @Transactional(readOnly = true)
    public List<AntivirusSignature> getByIdsEntities(List<UUID> ids) {
        Map<UUID, Integer> positions = new LinkedHashMap<>();
        for (int index = 0; index < ids.size(); index++) {
            positions.putIfAbsent(ids.get(index), index);
        }

        return signatureRepository.findAllById(ids).stream()
                .sorted(Comparator.comparingInt(signature -> positions.getOrDefault(signature.getId(), Integer.MAX_VALUE)))
                .toList();
    }

    private AntivirusSignature getEntity(UUID id) {
        return signatureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found"));
    }

    private void ensureExists(UUID id) {
        if (!signatureRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found");
        }
    }

    private void ensureNotDeleted(AntivirusSignature signature) {
        if (signature.getStatus() == AntivirusSignatureStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "deleted signatures cannot be updated");
        }
    }

    private void validateRequest(String firstBytesHex, String remainderHashHex, Long offsetStart, Long offsetEnd) {
        if (offsetEnd < offsetStart) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "offsetEnd must be greater than or equal to offsetStart");
        }
        if (!isHex(firstBytesHex)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "firstBytesHex must contain only hex characters");
        }
        if (!isHex(remainderHashHex)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "remainderHashHex must contain only hex characters");
        }
    }

    private boolean isHex(String value) {
        return value != null && !value.isBlank() && value.matches("^[0-9A-Fa-f]+$");
    }

    private void applyRequest(AntivirusSignature signature, AntivirusSignatureCreateRequest request) {
        signature.setThreatName(request.getThreatName());
        signature.setFirstBytesHex(request.getFirstBytesHex().toUpperCase());
        signature.setRemainderHashHex(request.getRemainderHashHex().toUpperCase());
        signature.setRemainderLength(request.getRemainderLength());
        signature.setFileType(request.getFileType());
        signature.setOffsetStart(request.getOffsetStart());
        signature.setOffsetEnd(request.getOffsetEnd());
    }

    private void applyRequest(AntivirusSignature signature, AntivirusSignatureUpdateRequest request) {
        signature.setThreatName(request.getThreatName());
        signature.setFirstBytesHex(request.getFirstBytesHex().toUpperCase());
        signature.setRemainderHashHex(request.getRemainderHashHex().toUpperCase());
        signature.setRemainderLength(request.getRemainderLength());
        signature.setFileType(request.getFileType());
        signature.setOffsetStart(request.getOffsetStart());
        signature.setOffsetEnd(request.getOffsetEnd());
    }

    private List<String> detectChangedFields(AntivirusSignature current, AntivirusSignatureUpdateRequest request) {
        List<String> changedFields = new ArrayList<>();
        if (!Objects.equals(current.getThreatName(), request.getThreatName())) {
            changedFields.add("threatName");
        }
        if (!Objects.equals(current.getFirstBytesHex(), request.getFirstBytesHex().toUpperCase())) {
            changedFields.add("firstBytesHex");
        }
        if (!Objects.equals(current.getRemainderHashHex(), request.getRemainderHashHex().toUpperCase())) {
            changedFields.add("remainderHashHex");
        }
        if (!Objects.equals(current.getRemainderLength(), request.getRemainderLength())) {
            changedFields.add("remainderLength");
        }
        if (!Objects.equals(current.getFileType(), request.getFileType())) {
            changedFields.add("fileType");
        }
        if (!Objects.equals(current.getOffsetStart(), request.getOffsetStart())) {
            changedFields.add("offsetStart");
        }
        if (!Objects.equals(current.getOffsetEnd(), request.getOffsetEnd())) {
            changedFields.add("offsetEnd");
        }
        return changedFields;
    }

    private void saveHistory(AntivirusSignature signature) {
        AntivirusSignatureHistory history = new AntivirusSignatureHistory();
        history.setSignature(signature);
        history.setVersionCreatedAt(now());
        history.setThreatName(signature.getThreatName());
        history.setFirstBytesHex(signature.getFirstBytesHex());
        history.setRemainderHashHex(signature.getRemainderHashHex());
        history.setRemainderLength(signature.getRemainderLength());
        history.setFileType(signature.getFileType());
        history.setOffsetStart(signature.getOffsetStart());
        history.setOffsetEnd(signature.getOffsetEnd());
        history.setUpdatedAt(signature.getUpdatedAt());
        history.setStatus(signature.getStatus());
        history.setDigitalSignatureBase64(signature.getDigitalSignatureBase64());
        history.setSourceBucket(signature.getSourceBucket());
        history.setSourceObjectKey(signature.getSourceObjectKey());
        history.setSourceOriginalFilename(signature.getSourceOriginalFilename());
        history.setSourceContentType(signature.getSourceContentType());
        history.setSourceSizeBytes(signature.getSourceSizeBytes());
        historyRepository.save(history);
    }

    private void saveAudit(AntivirusSignature signature, String actor, List<String> changedFields, String description) {
        AntivirusSignatureAudit audit = new AntivirusSignatureAudit();
        audit.setSignature(signature);
        audit.setChangedBy(actor == null || actor.isBlank() ? SYSTEM_ACTOR : actor);
        audit.setChangedAt(now());
        audit.setFieldsChanged(toFieldsChangedJson(changedFields));
        audit.setDescription(description);
        auditRepository.save(audit);
    }

    private String toFieldsChangedJson(List<String> changedFields) {
        try {
            return objectMapper.writeValueAsString(Map.of("changed", changedFields));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize changed fields", e);
        }
    }

    private List<String> allBusinessFields() {
        return List.of(
                "threatName",
                "firstBytesHex",
                "remainderHashHex",
                "remainderLength",
                "fileType",
                "offsetStart",
                "offsetEnd",
                "updatedAt",
                "status"
        );
    }

    private String signRecord(AntivirusSignature signature) {
        return signingService.sign(toPayload(signature));
    }

    private AntivirusSignaturePayload toPayload(AntivirusSignature signature) {
        return new AntivirusSignaturePayload(
                signature.getThreatName(),
                signature.getFirstBytesHex(),
                signature.getRemainderHashHex(),
                signature.getRemainderLength(),
                signature.getFileType(),
                signature.getOffsetStart(),
                signature.getOffsetEnd(),
                signature.getStatus()
        );
    }

    private AntivirusSignatureResponse toResponse(AntivirusSignature signature) {
        return new AntivirusSignatureResponse(
                signature.getId(),
                signature.getThreatName(),
                signature.getFirstBytesHex(),
                signature.getRemainderHashHex(),
                signature.getRemainderLength(),
                signature.getFileType(),
                signature.getOffsetStart(),
                signature.getOffsetEnd(),
                signature.getUpdatedAt(),
                signature.getStatus(),
                signature.getDigitalSignatureBase64()
        );
    }

    private LocalDateTime now() {
        return LocalDateTime.now().withNano(0);
    }
}
