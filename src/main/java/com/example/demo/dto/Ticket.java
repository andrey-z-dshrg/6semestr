// Ticket.java
package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Ticket {
    private LocalDateTime serverDate;      
    private long ticketLifetimeSeconds;
    private LocalDate activationDate;       
    private LocalDate expirationDate;     
    private Long userId;                  
    private Long deviceId;                  
    private boolean licenseBlocked;        

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

    public LocalDateTime getServerDate() { return serverDate; }
    public long getTicketLifetimeSeconds() { return ticketLifetimeSeconds; }
    public LocalDate getActivationDate() { return activationDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public Long getUserId() { return userId; }
    public Long getDeviceId() { return deviceId; }
    public boolean isLicenseBlocked() { return licenseBlocked; }
}
