package com.example.demo.signature;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "signature")
// Обычный конфигурационный класс для модуля подписи.
// Spring автоматически заполняет его значениями из application.properties по префиксу `signature.*`.
public class SignatureProperties {

    // Путь к keystore, из которого будут браться ключи подписи.
    private String keyStorePath;
    // Тип keystore. В учебном проекте по умолчанию используется PKCS12.
    private String keyStoreType = "PKCS12";
    // Пароль ко всему keystore.
    private String keyStorePassword;
    // Алиас конкретной записи внутри keystore.
    private String keyAlias;
    // Пароль приватного ключа. Если он не указан отдельно, проект использует пароль keystore.
    private String keyPassword;
    // При необходимости сюда можно положить публичный ключ отдельно от сертификата в keystore.
    private String publicKeyBase64;
    // Алгоритм подписи.
    private String algorithm = "SHA256withRSA";

    // Ниже идут обычные getters/setters, чтобы Spring мог заполнить этот объект из конфигурации.
    public String getKeyStorePath() {
        return keyStorePath;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    // Если отдельный пароль ключа не задан, используем пароль всего keystore.
    public String getKeyPassword() {
        return keyPassword != null ? keyPassword : keyStorePassword;
    }

    // Это просто значение из конфигурации, если оно задано.
    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    // Если алгоритм не задан явно, используем безопасное значение по умолчанию.
    public String getAlgorithm() {
        return algorithm != null ? algorithm : "SHA256withRSA";
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public void setPublicKeyBase64(String publicKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
