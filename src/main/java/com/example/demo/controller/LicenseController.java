package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.User;
import com.example.demo.model.License;
import com.example.demo.service.LicenseService;
import com.example.demo.signature.SigningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/licenses")
// Контроллер лицензий. Через него создают лицензию, активируют её на устройстве, продлевают ticket и проверяют подпись ticket.
public class LicenseController {

    // Зависимость, через которую контроллер передаёт работу дальше. Сам контроллер старается не решать бизнес-задачу, а только делегирует её в LicenseService.
    private final LicenseService licenseService;
    // Зависимость, через которую контроллер передаёт работу дальше. Сам контроллер старается не решать бизнес-задачу, а только делегирует её в SigningService.
    private final SigningService signingService;

    public LicenseController(LicenseService licenseService,
                             SigningService signingService) {
        this.licenseService = licenseService;
        this.signingService = signingService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    // Отдельный шаг логики внутри класса. Он решает одну локальную задачу и используется из других методов этого же класса.
    public ResponseEntity<License> createLicense(
            @RequestBody CreateLicenseRequest request,
            @AuthenticationPrincipal User admin) {
        License license = licenseService.createLicense(request, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(license);
    }

    @PostMapping("/activate")
    // Проверяет, можно ли активировать лицензию на этом устройстве, и если можно, формирует подписанный ticket.
    public ResponseEntity<TicketResponse> activateLicense(
            @RequestBody ActivateLicenseRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(licenseService.activateLicense(request, user.getId()));
    }

    @PostMapping("/renew")
    // Продлевает ticket или срок действия лицензии по правилам проекта.
    public ResponseEntity<TicketResponse> renewLicense(
            @RequestBody RenewLicenseRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(licenseService.renewLicense(request, user.getId()));
    }

    @PostMapping("/check")
    // Проверяет текущее состояние лицензии для устройства и возвращает свежий ticket.
    public ResponseEntity<TicketResponse> checkLicense(
            @RequestBody CheckLicenseRequest request,
            @AuthenticationPrincipal User user) {
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(licenseService.checkLicense(request, userId));
    }

    @PostMapping("/verify-ticket")
    // Проверяет подпись ticket, чтобы клиент мог убедиться, что ticket не подделан.
    public ResponseEntity<Map<String, Boolean>> verifyTicket(@RequestBody TicketResponse ticketResponse) {
        boolean valid = signingService.verify(ticketResponse.getTicket(), ticketResponse.getSignature());
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @GetMapping("/public-key")
    // Отдельный шаг логики внутри класса. Он решает одну локальную задачу и используется из других методов этого же класса.
    public ResponseEntity<String> getPublicKey() {
        return ResponseEntity.ok(signingService.getPublicKeyBase64());
    }

    @GetMapping("/public-key/pem")
    // Возвращает публичный ключ в PEM-формате, который удобно открывать в сторонних инструментах и библиотеках.
    public ResponseEntity<String> getPublicKeyPem() {
        return ResponseEntity.ok(signingService.getPublicKeyPem());
    }
}
