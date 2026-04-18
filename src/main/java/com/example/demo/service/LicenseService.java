package com.example.demo.service;

import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.dto.*;
import com.example.demo.entity.User;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.signature.SigningService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
// Главный сервис лицензий. Он следит за тем, кому принадлежит лицензия, на каких устройствах она активирована и когда можно выдать новый ticket.
public class LicenseService {

    // Одна из зависимостей сервиса. Через это поле сервис получает доступ к данным или к соседнему компоненту, который нужен для выполнения бизнес-логики.
    private final LicenseRepository licenseRepository;
    // Одна из зависимостей сервиса. Через это поле сервис получает доступ к данным или к соседнему компоненту, который нужен для выполнения бизнес-логики.
    private final ProductRepository productRepository;
    // Одна из зависимостей сервиса. Через это поле сервис получает доступ к данным или к соседнему компоненту, который нужен для выполнения бизнес-логики.
    private final LicenseTypeRepository licenseTypeRepository;
    // Одна из зависимостей сервиса. Через это поле сервис получает доступ к данным или к соседнему компоненту, который нужен для выполнения бизнес-логики.
    private final DeviceRepository deviceRepository;
    // Одна из зависимостей сервиса. Через это поле сервис получает доступ к данным или к соседнему компоненту, который нужен для выполнения бизнес-логики.
    private final DeviceLicenseRepository deviceLicenseRepository;
    // Одна из зависимостей сервиса. Через это поле сервис получает доступ к данным или к соседнему компоненту, который нужен для выполнения бизнес-логики.
    private final LicenseHistoryRepository licenseHistoryRepository;
    // Одна из зависимостей сервиса. Через это поле сервис получает доступ к данным или к соседнему компоненту, который нужен для выполнения бизнес-логики.
    private final UserRepository userRepository;
    // Одна из зависимостей сервиса. Через это поле сервис получает доступ к данным или к соседнему компоненту, который нужен для выполнения бизнес-логики.
    private final SigningService signingService;

    // Константа класса. Такие значения выносят отдельно, чтобы их было легко найти и не дублировать по коду.
    private static final long TICKET_LIFETIME_SECONDS = 3600;

    public LicenseService(LicenseRepository licenseRepository,
                          ProductRepository productRepository,
                          LicenseTypeRepository licenseTypeRepository,
                          DeviceRepository deviceRepository,
                          DeviceLicenseRepository deviceLicenseRepository,
                          LicenseHistoryRepository licenseHistoryRepository,
                          UserRepository userRepository,
                          SigningService signingService) {
        this.licenseRepository = licenseRepository;
        this.productRepository = productRepository;
        this.licenseTypeRepository = licenseTypeRepository;
        this.deviceRepository = deviceRepository;
        this.deviceLicenseRepository = deviceLicenseRepository;
        this.licenseHistoryRepository = licenseHistoryRepository;
        this.userRepository = userRepository;
        this.signingService = signingService;
    }

    @Transactional
    // Отдельный шаг логики внутри класса. Он решает одну локальную задачу и используется из других методов этого же класса.
    public License createLicense(CreateLicenseRequest request, Long adminId) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        LicenseType type = licenseTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "type not found"));
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "admin not found"));
        License license = new License();
        license.setCode(UUID.randomUUID().toString());
        license.setProduct(product);
        license.setType(type);
        license.setOwner(owner);
        license.setUser(null);
        license.setDeviceCount(request.getDeviceCount());
        license.setDescription(request.getDescription());
        license.setBlocked(false);
        licenseRepository.save(license);
        saveHistory(license, admin, "CREATED", "License created");
        return license;
    }

    @Transactional
    // Проверяет, можно ли активировать лицензию на этом устройстве, и если можно, формирует подписанный ticket.
    public TicketResponse activateLicense(ActivateLicenseRequest request, Long userId) {
        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));
        if (license.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "license is blocked");
        }
        if (license.getUser() != null && !license.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "license owned by another user");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseGet(() -> {
                    Device d = new Device();
                    d.setMacAddress(request.getDeviceMac());
                    d.setName(request.getDeviceName() != null ? request.getDeviceName() : request.getDeviceMac());
                    d.setUser(user);
                    return deviceRepository.save(d);
                });
        boolean isFirstActivation = license.getUser() == null;
        if (isFirstActivation) {
            license.setUser(user);
            license.setFirstActivationDate(LocalDate.now());
            license.setEndingDate(LocalDate.now().plusDays(license.getType().getDefaultDurationInDays()));
            licenseRepository.save(license);
            createDeviceLicense(license, device);
            saveHistory(license, user, "ACTIVATED", "First activation");
        } else {
            boolean alreadyActivated = deviceLicenseRepository
                    .existsByLicenseAndDevice_MacAddress(license, request.getDeviceMac());
            if (!alreadyActivated) {
                long activatedCount = deviceLicenseRepository.countByLicense(license);
                if (activatedCount >= license.getDeviceCount()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "device limit reached");
                }
                createDeviceLicense(license, device);
            }
            saveHistory(license, user, "ACTIVATED", "Re-activation on device " + request.getDeviceMac());
        }
        return buildTicketResponse(license, device);
    }

    @Transactional
    // Продлевает ticket или срок действия лицензии по правилам проекта.
    public TicketResponse renewLicense(RenewLicenseRequest request, Long userId) {
        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));
        if (license.getUser() == null || !license.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "license does not belong to this user");
        }
        if (license.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "license is blocked");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        boolean canRenew = license.getEndingDate() == null
                || license.getEndingDate().isBefore(LocalDate.now().plusDays(8));
        license.setEndingDate(
                (license.getEndingDate() != null ? license.getEndingDate() : LocalDate.now())
                        .plusDays(license.getType().getDefaultDurationInDays())
        );
        licenseRepository.save(license);
        saveHistory(license, user, "RENEWED", "License renewed");
        Device device = deviceLicenseRepository.findFirstByLicense(license)
                .map(DeviceLicense::getDevice)
                .orElse(null);
        return buildTicketResponse(license, device);
    }

    // Проверяет текущее состояние лицензии для устройства и возвращает свежий ticket.
    public TicketResponse checkLicense(CheckLicenseRequest request, Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));

        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "device not found"));

        License license;
        if (isAdmin || userId == null) {
            license = licenseRepository.findActiveByDeviceAndProduct(device, request.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));
        } else {
            license = licenseRepository.findActiveByDeviceUserAndProduct(device, userId, request.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found"));
        }
        return buildTicketResponse(license, device);
    }

    // Отдельный шаг логики внутри класса. Он решает одну локальную задачу и используется из других методов этого же класса.
    private void createDeviceLicense(License license, Device device) {
        DeviceLicense dl = new DeviceLicense();
        dl.setLicense(license);
        dl.setDevice(device);
        dl.setActivationDate(LocalDate.now());
        deviceLicenseRepository.save(dl);
    }

    // Сохраняет снимок текущего состояния записи в историю перед тем, как запись будет изменена или удалена.
    private void saveHistory(License license, User user, String status, String description) {
        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(user);
        history.setStatus(status);
        history.setChangeDate(LocalDateTime.now());
        history.setDescription(description);
        licenseHistoryRepository.save(history);
    }

    // Собирает объект ticket и сразу подписывает его, чтобы на выходе получился готовый защищённый ответ.
    private TicketResponse buildTicketResponse(License license, Device device) {
        LocalDateTime serverDate = LocalDateTime.now().withNano(0);
        Ticket ticket = new Ticket(
                serverDate,
                TICKET_LIFETIME_SECONDS,
                license.getFirstActivationDate(),
                license.getEndingDate(),
                license.getUser() != null ? license.getUser().getId() : null,
                device != null ? device.getId() : null,
                license.isBlocked()
        );
        String signature = signingService.sign(ticket);
        return new TicketResponse(ticket, signature);
    }
}
