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
import com.example.demo.model.AntivirusSignatureAuditAction;
import com.example.demo.model.AntivirusSignatureHistory;
import com.example.demo.model.AntivirusSignatureHistoryAction;
import com.example.demo.model.AntivirusSignatureStatus;
import com.example.demo.repository.AntivirusSignatureAuditRepository;
import com.example.demo.repository.AntivirusSignatureHistoryRepository;
import com.example.demo.repository.AntivirusSignatureRepository;
import com.example.demo.signature.SigningService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
// Главный сервис всей 4-й практической.
// Здесь сосредоточены все важные правила задания:
// создание сигнатуры, обновление сигнатуры, логическое удаление,
// запись в history, запись в audit, полная выгрузка, инкремент и проверка подписи.
public class AntivirusSignatureService {

    // Репозиторий основной таблицы сигнатур.
    // Через него сервис работает с текущим актуальным состоянием записи.
    private final AntivirusSignatureRepository signatureRepository;
    // Репозиторий таблицы history.
    // Сюда пишется слепок записи до update и до delete.
    private final AntivirusSignatureHistoryRepository historyRepository;
    // Репозиторий таблицы audit.
    // Здесь хранится не состояние записи, а факт действия над ней.
    private final AntivirusSignatureAuditRepository auditRepository;
    // Модуль электронной подписи.
    // Он нужен и для create/update/delete, и для отдельной операции verify.
    private final SigningService signingService;

    public AntivirusSignatureService(AntivirusSignatureRepository signatureRepository,
                                     AntivirusSignatureHistoryRepository historyRepository,
                                     AntivirusSignatureAuditRepository auditRepository,
                                     SigningService signingService) {
        this.signatureRepository = signatureRepository;
        this.historyRepository = historyRepository;
        this.auditRepository = auditRepository;
        this.signingService = signingService;
    }

    @Transactional(readOnly = true)
    // Полная выгрузка возвращает только рабочие сигнатуры.
    // По условию задания записи со статусом DELETED сюда намеренно не включаются.
    public List<AntivirusSignatureResponse> getFullExport() {
        return signatureRepository.findAllByStatusNotOrderByIdAsc(AntivirusSignatureStatus.DELETED).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    // Инкрементальная выгрузка возвращает все записи, изменённые после `since`.
    // Здесь DELETED-записи тоже важны, потому что клиент должен знать о логическом удалении.
    public List<AntivirusSignatureResponse> getIncrement(LocalDateTime since) {
        return signatureRepository.findAllByUpdatedAtAfterOrderByUpdatedAtAscIdAsc(since).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    // Возвращает только те сигнатуры, id которых пришли в запросе.
    // Это отдельная операция задания для выборочной загрузки.
    public List<AntivirusSignatureResponse> getByIds(List<Long> ids) {
        return signatureRepository.findAllById(ids).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    // Сценарий create не сводится к одному save().
    // Сначала создаётся запись без окончательной подписи, затем она получает id,
    // потом уже подписывается итоговое состояние и фиксируется событие CREATE в audit.
    public AntivirusSignatureResponse create(AntivirusSignatureCreateRequest request, String actor) {
        LocalDateTime now = now();

        AntivirusSignature signature = new AntivirusSignature();
        // Бизнес-данные приходят из DTO запроса.
        signature.setSignatureName(request.getSignatureName());
        signature.setMalwareName(request.getMalwareName());
        signature.setSignatureBody(request.getSignatureBody());
        signature.setDescription(request.getDescription());
        // Новая запись сразу считается активной.
        signature.setStatus(AntivirusSignatureStatus.ACTIVE);
        // При создании createdAt и updatedAt совпадают.
        signature.setCreatedAt(now);
        signature.setUpdatedAt(now);
        // Временное значение нужно до вычисления реальной подписи.
        signature.setDigitalSignature("PENDING");

        // После saveAndFlush сущность уже имеет id, а значит можно подписывать полное состояние записи.
        signature = signatureRepository.saveAndFlush(signature);
        signature.setDigitalSignature(signRecord(signature));
        signature = signatureRepository.save(signature);

        // Для create пишем только audit: history тут нет, потому что старой версии ещё не существовало.
        saveAudit(signature, AntivirusSignatureAuditAction.CREATE, actor, "Signature created");
        return toResponse(signature);
    }

    @Transactional
    // Обновление обязано выполнить сразу три требования задания:
    // сохранить прошлую версию в history, пересчитать подпись и записать UPDATE в audit.
    public AntivirusSignatureResponse update(Long id, AntivirusSignatureUpdateRequest request, String actor) {
        AntivirusSignature signature = getEntity(id);
        // Старое состояние сохраняем до применения новых полей.
        saveHistory(signature, AntivirusSignatureHistoryAction.UPDATE);

        signature.setSignatureName(request.getSignatureName());
        signature.setMalwareName(request.getMalwareName());
        signature.setSignatureBody(request.getSignatureBody());
        signature.setDescription(request.getDescription());
        // updatedAt должен показать момент новой версии записи.
        signature.setUpdatedAt(now());
        // Подпись обязана соответствовать уже обновлённым данным.
        signature.setDigitalSignature(signRecord(signature));

        AntivirusSignature saved = signatureRepository.save(signature);
        saveAudit(saved, AntivirusSignatureAuditAction.UPDATE, actor, "Signature updated");
        return toResponse(saved);
    }

    @Transactional
    // Удаление логическое: строка не исчезает из базы, а просто меняет статус.
    // Благодаря этому запись остаётся доступной для инкремента, истории и проверки преподавателем.
    public AntivirusSignatureResponse delete(Long id, String actor) {
        AntivirusSignature signature = getEntity(id);
        // Повторное удаление считаем ошибкой сценария.
        if (signature.getStatus() == AntivirusSignatureStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "signature already deleted");
        }

        // До изменения статуса сохраняем прошлое состояние в history.
        saveHistory(signature, AntivirusSignatureHistoryAction.DELETE);

        signature.setStatus(AntivirusSignatureStatus.DELETED);
        signature.setUpdatedAt(now());
        // Статус тоже входит в подписываемое состояние, поэтому подпись пересчитывается и здесь.
        signature.setDigitalSignature(signRecord(signature));

        AntivirusSignature saved = signatureRepository.save(signature);
        saveAudit(saved, AntivirusSignatureAuditAction.DELETE, actor, "Signature deleted logically");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    // Возвращает таблицу history по одной сигнатуре.
    // Здесь хранятся прошлые состояния записи, а не просто факты действий.
    public List<AntivirusSignatureHistoryResponse> getHistory(Long signatureId) {
        ensureExists(signatureId);
        return historyRepository.findAllBySignature_IdOrderByHistoryCreatedAtDescIdDesc(signatureId).stream()
                .map(history -> new AntivirusSignatureHistoryResponse(
                        history.getId(),
                        history.getSignature().getId(),
                        history.getAction(),
                        history.getSignatureName(),
                        history.getMalwareName(),
                        history.getSignatureBody(),
                        history.getDescription(),
                        history.getStatus(),
                        history.getDigitalSignature(),
                        history.getOriginalCreatedAt(),
                        history.getOriginalUpdatedAt(),
                        history.getHistoryCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    // Возвращает таблицу audit по одной сигнатуре.
    // Здесь важны исполнитель, тип действия и время события.
    public List<AntivirusSignatureAuditResponse> getAudit(Long signatureId) {
        ensureExists(signatureId);
        return auditRepository.findAllBySignature_IdOrderByActionAtDescIdDesc(signatureId).stream()
                .map(audit -> new AntivirusSignatureAuditResponse(
                        audit.getId(),
                        audit.getSignature().getId(),
                        audit.getAction(),
                        audit.getActor(),
                        audit.getDetails(),
                        audit.getActionAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    // Проверяет целостность текущей версии записи.
    // Берётся payload из текущих полей и сравнивается с digitalSignature, сохранённой в базе.
    public SignatureVerificationResponse verify(Long id) {
        AntivirusSignature signature = getEntity(id);
        return new SignatureVerificationResponse(
                signingService.verify(toPayload(signature), signature.getDigitalSignature())
        );
    }

    // Унифицированный способ получить сущность или сразу отдать 404.
    private AntivirusSignature getEntity(Long id) {
        return signatureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found"));
    }

    // Более лёгкая проверка существования по id, когда сама сущность не нужна.
    private void ensureExists(Long id) {
        if (!signatureRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found");
        }
    }

    // Записывает в history ровно то состояние, которое было у записи до изменения.
    // Поэтому этот метод всегда вызывается до update/delete основной сущности.
    private void saveHistory(AntivirusSignature signature, AntivirusSignatureHistoryAction action) {
        AntivirusSignatureHistory history = new AntivirusSignatureHistory();
        history.setSignature(signature);
        history.setAction(action);
        history.setSignatureName(signature.getSignatureName());
        history.setMalwareName(signature.getMalwareName());
        history.setSignatureBody(signature.getSignatureBody());
        history.setDescription(signature.getDescription());
        history.setStatus(signature.getStatus());
        history.setDigitalSignature(signature.getDigitalSignature());
        history.setOriginalCreatedAt(signature.getCreatedAt());
        history.setOriginalUpdatedAt(signature.getUpdatedAt());
        history.setHistoryCreatedAt(now());
        historyRepository.save(history);
    }

    // Audit фиксирует не слепок данных, а факт действия.
    // Здесь сохраняется тип операции, исполнитель и служебное пояснение.
    private void saveAudit(AntivirusSignature signature,
                           AntivirusSignatureAuditAction action,
                           String actor,
                           String details) {
        AntivirusSignatureAudit audit = new AntivirusSignatureAudit();
        audit.setSignature(signature);
        audit.setAction(action);
        audit.setActor(actor == null || actor.isBlank() ? "system" : actor);
        audit.setDetails(details);
        audit.setActionAt(now());
        auditRepository.save(audit);
    }

    // Подписываем не саму JPA-сущность, а специальный payload-объект.
    // Это уменьшает риск случайно включить в подпись лишние поля или JPA-детали.
    private String signRecord(AntivirusSignature signature) {
        return signingService.sign(toPayload(signature));
    }

    // Собирает тот набор полей, который считается официальным состоянием сигнатуры для подписи.
    // Любое изменение этих полей должно привести к изменению digitalSignature.
    private AntivirusSignaturePayload toPayload(AntivirusSignature signature) {
        return new AntivirusSignaturePayload(
                signature.getId(),
                signature.getSignatureName(),
                signature.getMalwareName(),
                signature.getSignatureBody(),
                signature.getDescription(),
                signature.getStatus(),
                signature.getCreatedAt(),
                signature.getUpdatedAt()
        );
    }

    // Преобразует внутреннюю JPA-сущность в DTO ответа для клиента.
    private AntivirusSignatureResponse toResponse(AntivirusSignature signature) {
        return new AntivirusSignatureResponse(
                signature.getId(),
                signature.getSignatureName(),
                signature.getMalwareName(),
                signature.getSignatureBody(),
                signature.getDescription(),
                signature.getStatus(),
                signature.getDigitalSignature(),
                signature.getCreatedAt(),
                signature.getUpdatedAt()
        );
    }

    // Убираем наносекунды, чтобы timestamps были стабильнее в JSON и тестах.
    private LocalDateTime now() {
        return LocalDateTime.now().withNano(0);
    }
}
