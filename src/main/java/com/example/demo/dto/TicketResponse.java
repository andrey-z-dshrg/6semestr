package com.example.demo.dto;

// DTO-класс для обмена данными между клиентом, контроллером и сервисом. В нём нет сложной логики: он нужен, чтобы удобно переносить данные.
public class TicketResponse {
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private Ticket ticket;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private String signature;

    public TicketResponse() {
    }

    public TicketResponse(Ticket ticket, String signature) {
        this.ticket = ticket;
        this.signature = signature;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public Ticket getTicket() {
        return ticket;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getSignature() {
        return signature;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
