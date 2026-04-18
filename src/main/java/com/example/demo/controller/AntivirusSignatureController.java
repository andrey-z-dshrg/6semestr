package com.example.demo.controller;

import com.example.demo.dto.AntivirusSignatureAuditResponse;
import com.example.demo.dto.AntivirusSignatureCreateRequest;
import com.example.demo.dto.AntivirusSignatureHistoryResponse;
import com.example.demo.dto.AntivirusSignatureResponse;
import com.example.demo.dto.AntivirusSignatureUpdateRequest;
import com.example.demo.dto.SignatureIdsRequest;
import com.example.demo.dto.SignatureVerificationResponse;
import com.example.demo.entity.User;
import com.example.demo.service.AntivirusSignatureService;
import com.example.demo.signature.SigningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/signatures")
// Этот контроллер является HTTP-входом в модуль сигнатур из 4-й практической.
// Он принимает запросы клиента, извлекает из них параметры и передаёт управление в сервис,
// где уже выполняются проверки правил задания, работа с таблицами history/audit и пересчёт подписи.
public class AntivirusSignatureController {

    // Основной сервис сигнатур.
    // Контроллер специально не решает бизнес-задачу сам, чтобы вся логика create/update/delete жила в одном месте.
    private final AntivirusSignatureService antivirusSignatureService;
    // Сервис подписи нужен здесь только для выдачи публичного ключа.
    // Подписывать и проверять записи через него напрямую контроллер не будет: это делает сервисный слой.
    private final SigningService signingService;

    public AntivirusSignatureController(AntivirusSignatureService antivirusSignatureService,
                                        SigningService signingService) {
        this.antivirusSignatureService = antivirusSignatureService;
        this.signingService = signingService;
    }

    @GetMapping
    // Полная выгрузка возвращает рабочую базу сигнатур целиком.
    // По условию практической логически удалённые записи со статусом DELETED здесь специально не показываются.
    public ResponseEntity<List<AntivirusSignatureResponse>> getFullExport() {
        return ResponseEntity.ok(antivirusSignatureService.getFullExport());
    }

    @GetMapping("/increment")
    // Инкрементальная выгрузка нужна для синхронизации после момента `since`.
    // В отличие от полной выгрузки, здесь важно вернуть и DELETED-записи тоже,
    // чтобы клиент понял, какие сигнатуры у себя нужно пометить как удалённые.
    public ResponseEntity<List<AntivirusSignatureResponse>> getIncrement(@RequestParam LocalDateTime since) {
        return ResponseEntity.ok(antivirusSignatureService.getIncrement(since));
    }

    @PostMapping("/by-ids")
    // Этот endpoint позволяет запросить не всю базу, а только конкретный набор сигнатур по их id.
    // Такой режим удобен, когда клиент уже знает, какие записи ему нужны.
    public ResponseEntity<List<AntivirusSignatureResponse>> getByIds(@Valid @RequestBody SignatureIdsRequest request) {
        return ResponseEntity.ok(antivirusSignatureService.getByIds(request.getIds()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    // Создание сигнатуры разрешено только администратору.
    // После вызова сервис не просто сохраняет запись, а ещё считает электронную подпись и пишет событие в аудит.
    public ResponseEntity<AntivirusSignatureResponse> create(@Valid @RequestBody AntivirusSignatureCreateRequest request,
                                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(antivirusSignatureService.create(request, actor(user)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    // Обновление тоже доступно только администратору.
    // Внутри сервис сначала сохранит старое состояние в history, затем применит новые поля,
    // пересчитает подпись и зафиксирует сам факт обновления в audit.
    public ResponseEntity<AntivirusSignatureResponse> update(@PathVariable Long id,
                                                             @Valid @RequestBody AntivirusSignatureUpdateRequest request,
                                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(antivirusSignatureService.update(id, request, actor(user)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    // Удаление в этой работе логическое.
    // То есть запись остаётся в базе, но переводится в статус DELETED, чтобы её можно было увидеть в истории и инкременте.
    public ResponseEntity<AntivirusSignatureResponse> delete(@PathVariable Long id,
                                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(antivirusSignatureService.delete(id, actor(user)));
    }

    @GetMapping("/{id}/history")
    // Возвращает прошлые версии одной сигнатуры из таблицы history.
    // Именно этот endpoint удобно использовать на защите, чтобы показать, что update и delete сохраняют старое состояние.
    public ResponseEntity<List<AntivirusSignatureHistoryResponse>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(antivirusSignatureService.getHistory(id));
    }

    @GetMapping("/{id}/audit")
    // Возвращает журнал действий над сигнатурой.
    // Здесь видно не старые данные записи, а кто, что и когда с ней сделал.
    public ResponseEntity<List<AntivirusSignatureAuditResponse>> getAudit(@PathVariable Long id) {
        return ResponseEntity.ok(antivirusSignatureService.getAudit(id));
    }

    @PostMapping("/{id}/verify")
    // Проверка подписи отвечает на вопрос: совпадает ли текущее содержимое записи
    // с тем состоянием, которое когда-то было подписано и сохранено в digitalSignature.
    public ResponseEntity<SignatureVerificationResponse> verify(@PathVariable Long id) {
        return ResponseEntity.ok(antivirusSignatureService.verify(id));
    }

    @GetMapping("/public-key")
    // Отдаёт публичный ключ в Base64-форме.
    // Он нужен, если подпись хотят проверить вне приложения, например в Postman-скрипте или внешнем коде.
    public ResponseEntity<String> getPublicKey() {
        return ResponseEntity.ok(signingService.getPublicKeyBase64());
    }

    @GetMapping("/public-key/pem")
    // Отдаёт тот же публичный ключ, но в PEM-виде.
    // Такой формат лучше подходит для внешних криптографических инструментов и библиотек.
    public ResponseEntity<String> getPublicKeyPem() {
        return ResponseEntity.ok(signingService.getPublicKeyPem());
    }

    // Преобразует объект текущего пользователя в строку для аудита.
    // Если по каким-то причинам пользователя нет, в audit запишется "system".
    private String actor(User user) {
        return user != null ? user.getUsername() : "system";
    }
}
