package com.example.demo.dto;

// DTO-класс для обмена данными между клиентом, контроллером и сервисом. В нём нет сложной логики: он нужен, чтобы удобно переносить данные.
public class TokenPairResponse {
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private String accessToken;
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private String refreshToken;
    private String tokenType = "Bearer";
    // Поле DTO. В него приходит часть JSON-запроса или из него собирается JSON-ответ.
    private long accessTokenExpiresIn;

    public TokenPairResponse(String accessToken, String refreshToken, long accessTokenExpiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getAccessToken() {
        return accessToken;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getRefreshToken() {
        return refreshToken;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public String getTokenType() {
        return tokenType;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public long getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }

    // Метод DTO, который помогает читать или записывать поля объекта при преобразовании JSON и работе сервиса.
    public void setAccessTokenExpiresIn(long accessTokenExpiresIn) {
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }
}
