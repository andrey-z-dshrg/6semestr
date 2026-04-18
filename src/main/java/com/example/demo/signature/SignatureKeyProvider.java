package com.example.demo.signature;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

@Component
// Этот класс инкапсулирует всю работу с keystore.
// Остальные части проекта не должны знать, где лежит ключ, как его открыть
// и по какому алиасу его искать: они просто просят готовый PrivateKey/PublicKey.
public class SignatureKeyProvider {

    // Конфигурация, в которой лежат путь к keystore, пароли, алиас и опциональный публичный ключ.
    private final SignatureProperties properties;
    // Блокировка нужна, чтобы два потока не начали одновременно загружать один и тот же keystore.
    private final ReentrantLock lock = new ReentrantLock();

    // Приватный ключ используется только для подписи.
    private volatile PrivateKey privateKey;
    // Публичный ключ используется только для проверки подписи.
    private volatile PublicKey publicKey;

    public SignatureKeyProvider(SignatureProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    // Инициализация при старте приложения.
    // Если keystore настроен неправильно, лучше узнать об этом сразу, а не на первом реальном запросе.
    public void init() {
        loadKeys();
    }

    // Основная загрузка ключей из keystore.
    // Метод аккуратно обрабатывает и ситуацию, когда ключи уже загружены, и ситуацию ошибки конфигурации.
    private void loadKeys() {
        lock.lock();
        try {
            if (privateKey != null && publicKey != null) {
                return;
            }

            KeyStore keyStore = KeyStore.getInstance(properties.getKeyStoreType());
            Resource resource = new DefaultResourceLoader().getResource(properties.getKeyStorePath());

            keyStore.load(
                    resource.getInputStream(),
                    properties.getKeyStorePassword().toCharArray()
            );

            privateKey = (PrivateKey) keyStore.getKey(
                    properties.getKeyAlias(),
                    properties.getKeyPassword().toCharArray()
            );

            if (privateKey == null) {
                throw new IllegalStateException(
                        "Private key not found for alias: " + properties.getKeyAlias());
            }

            Certificate cert = keyStore.getCertificate(properties.getKeyAlias());
            if (cert == null) {
                throw new IllegalStateException(
                        "Certificate not found for alias: " + properties.getKeyAlias());
            }

            publicKey = resolveConfiguredPublicKey(cert.getPublicKey());

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load keystore: " + e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    // Если публичный ключ явно задан в конфигурации, используем его.
    // Если нет, берём ключ из сертификата, лежащего рядом с приватным ключом в keystore.
    private PublicKey resolveConfiguredPublicKey(PublicKey fallbackPublicKey) {
        String configuredPublicKey = properties.getPublicKeyBase64();
        if (!StringUtils.hasText(configuredPublicKey)) {
            return fallbackPublicKey;
        }

        try {
            String normalized = configuredPublicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(normalized);
            return KeyFactory.getInstance(fallbackPublicKey.getAlgorithm())
                    .generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse configured public key: " + e.getMessage(), e);
        }
    }

    // Ленивая выдача приватного ключа:
    // если по какой-то причине объект ещё не инициализирован, ключ будет загружен прямо здесь.
    public PrivateKey getPrivateKey() {
        if (privateKey == null) {
            loadKeys();
        }
        return privateKey;
    }

    // Ленивая выдача публичного ключа по тому же принципу, что и для приватного.
    public PublicKey getPublicKey() {
        if (publicKey == null) {
            loadKeys();
        }
        return publicKey;
    }
}
