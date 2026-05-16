package com.example.demo.controller;

import com.example.demo.dto.AntivirusSignatureAuditResponse;
import com.example.demo.dto.AntivirusSignatureCreateRequest;
import com.example.demo.dto.AntivirusSignatureHistoryResponse;
import com.example.demo.dto.AntivirusSignatureResponse;
import com.example.demo.dto.AntivirusSignatureUpdateRequest;
import com.example.demo.dto.SignatureFileUploadResponse;
import com.example.demo.dto.SignatureFileUrlResponse;
import com.example.demo.dto.SignatureIdsRequest;
import com.example.demo.dto.SignatureVerificationResponse;
import com.example.demo.entity.User;
import com.example.demo.service.AntivirusSignatureService;
import com.example.demo.service.SignatureFileManagementService;
import com.example.demo.signature.SigningService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/signatures")
// Это обычный JSON API модуля сигнатур.
// Через него администратор управляет записями, а клиент и преподаватель могут читать выгрузки, историю, аудит и результат проверки подписи.
public class AntivirusSignatureController {

    private final AntivirusSignatureService antivirusSignatureService;
    private final SignatureFileManagementService signatureFileManagementService;
    private final SigningService signingService;

    public AntivirusSignatureController(AntivirusSignatureService antivirusSignatureService,
                                        SignatureFileManagementService signatureFileManagementService,
                                        SigningService signingService) {
        this.antivirusSignatureService = antivirusSignatureService;
        this.signatureFileManagementService = signatureFileManagementService;
        this.signingService = signingService;
    }

    @GetMapping
    // Полная выгрузка по условию должна содержать только ACTUAL.
    public ResponseEntity<List<AntivirusSignatureResponse>> getFullExport() {
        return ResponseEntity.ok(antivirusSignatureService.getFullExport());
    }

    @GetMapping("/increment")
    // Инкремент показывает всё, что менялось после since, включая DELETED.
    public ResponseEntity<List<AntivirusSignatureResponse>> getIncrement(@RequestParam LocalDateTime since) {
        return ResponseEntity.ok(antivirusSignatureService.getIncrement(since));
    }

    @PostMapping("/by-ids")
    // Этот endpoint удобен, когда клиенту нужна не вся база, а только конкретные сигнатуры по UUID.
    public ResponseEntity<List<AntivirusSignatureResponse>> getByIds(@Valid @RequestBody SignatureIdsRequest request) {
        return ResponseEntity.ok(antivirusSignatureService.getByIds(request.getIds()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    // Создавать сигнатуры может только администратор, потому что эта операция меняет рабочую базу.
    public ResponseEntity<AntivirusSignatureResponse> create(@Valid @RequestBody AntivirusSignatureCreateRequest request,
                                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(antivirusSignatureService.create(request, actor(user)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SignatureFileUploadResponse> upload(@RequestPart("file") MultipartFile file,
                                                              @RequestParam(name = "threatName", required = false) String threatName,
                                                              @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(signatureFileManagementService.upload(file, threatName, actor(user)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/files/presigned-urls/by-ids")
    public ResponseEntity<List<SignatureFileUrlResponse>> getPresignedUrlsByIds(@Valid @RequestBody SignatureIdsRequest request) {
        return ResponseEntity.ok(signatureFileManagementService.getPresignedUrlsByIds(request.getIds()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    // При обновлении сервис сам сохранит старую версию в history и заново посчитает подпись.
    public ResponseEntity<AntivirusSignatureResponse> update(@PathVariable UUID id,
                                                             @Valid @RequestBody AntivirusSignatureUpdateRequest request,
                                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(antivirusSignatureService.update(id, request, actor(user)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    // Delete здесь логический: запись не пропадает физически, а меняет статус на DELETED.
    public ResponseEntity<AntivirusSignatureResponse> delete(@PathVariable UUID id,
                                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(antivirusSignatureService.delete(id, actor(user)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/history")
    // История нужна прежде всего для проверки, что update и delete сохраняют старое состояние записи.
    public ResponseEntity<List<AntivirusSignatureHistoryResponse>> getHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(antivirusSignatureService.getHistory(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/audit")
    // Аудит показывает уже не старые данные, а сами действия над сигнатурой.
    public ResponseEntity<List<AntivirusSignatureAuditResponse>> getAudit(@PathVariable UUID id) {
        return ResponseEntity.ok(antivirusSignatureService.getAudit(id));
    }

    @PostMapping("/{id}/verify")
    // Проверка подписи отвечает на простой вопрос: не расходятся ли текущие поля записи с сохранённой подписью.
    public ResponseEntity<SignatureVerificationResponse> verify(@PathVariable UUID id) {
        return ResponseEntity.ok(antivirusSignatureService.verify(id));
    }

    @GetMapping("/public-key")
    // Публичный ключ можно безопасно отдавать наружу, потому что им проверяют подпись, а не создают её.
    public ResponseEntity<String> getPublicKey() {
        return ResponseEntity.ok(signingService.getPublicKeyBase64());
    }

    @GetMapping("/public-key/pem")
    // PEM-вид удобен для внешних инструментов, например для ручной проверки вне приложения.
    public ResponseEntity<String> getPublicKeyPem() {
        return ResponseEntity.ok(signingService.getPublicKeyPem());
    }

    private String actor(User user) {
        return user != null ? user.getUsername() : "system";
    }
}
