package com.example.demo.signature;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Base64;

@Service
// Это общий вход в криптографию для проекта.
// Остальному коду не нужно знать детали Java Signature API: сервис либо подписывает данные, либо проверяет их.
public class SigningService {

    private final SignatureKeyProvider keyProvider;
    private final JsonCanonicalizationService canonicalizationService;
    private final SignatureProperties properties;

    public SigningService(SignatureKeyProvider keyProvider,
                          JsonCanonicalizationService canonicalizationService,
                          SignatureProperties properties) {
        this.keyProvider = keyProvider;
        this.canonicalizationService = canonicalizationService;
        this.properties = properties;
    }

    // Этот метод нужен для обычной бизнес-логики, где у нас есть объект с полями, а не готовый бинарный документ.
    public String sign(Object payload) {
        byte[] signatureBytes = sign(canonicalizationService.canonicalize(payload));
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    // Этот перегруженный метод нужен уже для задания 5.
    // Им подписывается не объект, а готовый массив байтов, например собранный manifest.bin до добавления подписи.
    public byte[] sign(byte[] payloadBytes) {
        try {
            Signature signer = Signature.getInstance(properties.getAlgorithm());
            signer.initSign(keyProvider.getPrivateKey());
            signer.update(payloadBytes);
            return signer.sign();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Signing failed: " + e.getMessage(), e);
        }
    }

    public boolean verify(Object payload, String signatureBase64) {
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
            return false;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Verification failed: " + e.getMessage(), e);
        }
    }

    // Такой вариант удобен в тестах и при проверке бинарных документов, когда подпись уже хранится как массив байтов.
    public boolean verify(byte[] payloadBytes, byte[] signatureBytes) {
        try {
            Signature verifier = Signature.getInstance(properties.getAlgorithm());
            verifier.initVerify(keyProvider.getPublicKey());
            verifier.update(payloadBytes);
            return verifier.verify(signatureBytes);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Verification failed: " + e.getMessage(), e);
        }
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyProvider.getPublicKey().getEncoded());
    }

    public String getPublicKeyPem() {
        String base64 = getPublicKeyBase64();
        String wrapped = base64.replaceAll("(.{64})", "$1\n");
        if (!wrapped.endsWith("\n")) {
            wrapped += "\n";
        }
        return "-----BEGIN PUBLIC KEY-----\n" + wrapped + "-----END PUBLIC KEY-----";
    }
}
