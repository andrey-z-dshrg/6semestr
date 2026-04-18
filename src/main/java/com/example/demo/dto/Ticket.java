package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

// DTO-класс для обмена данными между клиентом, контроллером и сервисом. В нём нет сложной логики: он нужен, чтобы удобно переносить данные.
public class Ticket {
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private LocalDateTime serverDate;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private long ticketLifetimeSeconds;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private LocalDate activationDate;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private LocalDate expirationDate;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private Long userId;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private Long deviceId;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private boolean licenseBlocked;

    public Ticket() {
    }

    public Ticket(LocalDateTime serverDate, long ticketLifetimeSeconds,
                  LocalDate activationDate, LocalDate expirationDate,
                  Long userId, Long deviceId, boolean licenseBlocked) {
        this.serverDate = serverDate;
        this.ticketLifetimeSeconds = ticketLifetimeSeconds;
        this.activationDate = activationDate;
        this.expirationDate = expirationDate;
        this.userId = userId;
        this.deviceId = deviceId;
        this.licenseBlocked = licenseBlocked;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public LocalDateTime getServerDate() {
        return serverDate;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setServerDate(LocalDateTime serverDate) {
        this.serverDate = serverDate;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public long getTicketLifetimeSeconds() {
        return ticketLifetimeSeconds;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setTicketLifetimeSeconds(long ticketLifetimeSeconds) {
        this.ticketLifetimeSeconds = ticketLifetimeSeconds;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public LocalDate getActivationDate() {
        return activationDate;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setActivationDate(LocalDate activationDate) {
        this.activationDate = activationDate;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public Long getUserId() {
        return userId;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public Long getDeviceId() {
        return deviceId;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public boolean isLicenseBlocked() {
        return licenseBlocked;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setLicenseBlocked(boolean licenseBlocked) {
        this.licenseBlocked = licenseBlocked;
    }
}
