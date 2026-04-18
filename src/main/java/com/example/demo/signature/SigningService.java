package com.example.demo.signature;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Base64;

@Service
// Сервис подписи - это общий вход в криптографию для всего проекта.
// Внешний код не должен знать детали Java Signature API: ему достаточно передать объект
// и получить либо строку подписи, либо результат проверки.
public class SigningService {

    // Отсюда сервис берёт приватный и публичный ключ.
    // Отдельный provider позволяет не смешивать чтение keystore с логикой подписи.
    private final SignatureKeyProvider keyProvider;
    // Перед подписью объект нужно привести к стабильному JSON-виду,
    // иначе одинаковые данные могут дать разную подпись из-за разного порядка полей.
    private final JsonCanonicalizationService canonicalizationService;
    // Хранит алгоритм подписи и связанные настройки.
    private final SignatureProperties properties;

    public SigningService(SignatureKeyProvider keyProvider,
                          JsonCanonicalizationService canonicalizationService,
                          SignatureProperties properties) {
        this.keyProvider = keyProvider;
        this.canonicalizationService = canonicalizationService;
        this.properties = properties;
    }

    // Формирует подпись в три шага:
    // 1. канонизирует объект в стабильный набор байтов;
    // 2. подписывает эти байты приватным ключом;
    // 3. кодирует подпись в Base64, чтобы её было удобно хранить в БД и передавать по HTTP.
    public String sign(Object payload) {
        try {
            byte[] canonicalBytes = canonicalizationService.canonicalize(payload);

            Signature signer = Signature.getInstance(properties.getAlgorithm());
            signer.initSign(keyProvider.getPrivateKey());
            signer.update(canonicalBytes);
            byte[] signatureBytes = signer.sign();

            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Signing failed: " + e.getMessage(), e);
        }
    }

    // Проверка работает зеркально по отношению к sign():
    // те же данные снова канонизируются, подпись декодируется из Base64,
    // после чего публичным ключом проверяется, подходит ли подпись к этим данным.
    public boolean verify(Object payload, String signatureBase64) {
        // Пустую или отсутствующую подпись сразу считаем невалидной.
        if (!StringUtils.hasText(signatureBase64)) {
            return false;
        }

        try {
            byte[] canonicalBytes = canonicalizationService.canonicalize(payload);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            Signature verifier = Signature.getInstance(properties.getAlgorithm());
            verifier.initVerify(keyProvider.getPublicKey());
            verifier.update(canonicalBytes);

            return verifier.verify(signatureBytes);
        } catch (IllegalArgumentException e) {
            // Сюда попадём, если строка подписи не декодируется как корректный Base64.
            return false;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Verification failed: " + e.getMessage(), e);
        }
    }

    // Публичный ключ можно безопасно отдавать наружу:
    // им можно проверять подписи, но нельзя подписывать новые данные.
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyProvider.getPublicKey().getEncoded());
    }

    // PEM - это более привычный формат для внешних инструментов.
    // Такой ключ легко использовать в OpenSSL, сторонних библиотеках и скриптах проверки.
    public String getPublicKeyPem() {
        String base64 = getPublicKeyBase64();
        String wrapped = base64.replaceAll("(.{64})", "$1\n");
        if (!wrapped.endsWith("\n")) {
            wrapped += "\n";
        }
        return "-----BEGIN PUBLIC KEY-----\n" + wrapped + "-----END PUBLIC KEY-----";
    }
}
