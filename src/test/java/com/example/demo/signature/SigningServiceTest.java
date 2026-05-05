package com.example.demo.signature;

import com.example.demo.dto.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// Это набор unit-тестов для модуля подписи.
// Здесь мы отдельно убеждаемся, что подпись работает и для обычных JSON-объектов, и для готовых массивов байтов, которые понадобились в задании 5.
class SigningServiceTest {

    private SigningService signingService;

    @BeforeEach
    void setUp() {
        SignatureProperties properties = new SignatureProperties();
        properties.setKeyStorePath("classpath:keystore/quiz-server-keystore.p12");
        properties.setKeyStoreType("PKCS12");
        properties.setKeyStorePassword("ServerPass_1BIB23263");
        properties.setKeyAlias("makarovServer");
        properties.setKeyPassword("ServerPass_1BIB23263");
        properties.setAlgorithm("SHA256withRSA");

        SignatureKeyProvider keyProvider = new SignatureKeyProvider(properties);
        keyProvider.init();

        signingService = new SigningService(keyProvider, new JsonCanonicalizationService(), properties);
    }

    @Test
    void shouldSignAndVerifyTicket() {
        Ticket ticket = new Ticket(
                LocalDateTime.of(2026, 4, 4, 12, 30, 15),
                3600,
                LocalDate.of(2026, 4, 4),
                LocalDate.of(2026, 5, 4),
                10L,
                20L,
                false
        );

        String signature = signingService.sign(ticket);

        assertThat(signature).isNotBlank();
        assertThat(signingService.verify(ticket, signature)).isTrue();
    }

    @Test
    void shouldSignAndVerifyRawBytes() {
        byte[] document = "manifest bytes for task 5".getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = signingService.sign(document);

        assertThat(signatureBytes).isNotEmpty();
        assertThat(signingService.verify(document, signatureBytes)).isTrue();
    }

    @Test
    void shouldRejectTamperedTicket() {
        Ticket originalTicket = new Ticket(
                LocalDateTime.of(2026, 4, 4, 12, 30, 15),
                3600,
                LocalDate.of(2026, 4, 4),
                LocalDate.of(2026, 5, 4),
                10L,
                20L,
                false
        );

        String signature = signingService.sign(originalTicket);

        Ticket tamperedTicket = new Ticket(
                originalTicket.getServerDate(),
                originalTicket.getTicketLifetimeSeconds(),
                originalTicket.getActivationDate(),
                originalTicket.getExpirationDate(),
                999L,
                originalTicket.getDeviceId(),
                originalTicket.isLicenseBlocked()
        );

        assertThat(signingService.verify(tamperedTicket, signature)).isFalse();
    }

    @Test
    void shouldVerifyEquivalentJsonWithDifferentFieldOrder() {
        Map<String, Object> payload1 = new LinkedHashMap<>();
        payload1.put("deviceId", 20);
        payload1.put("userId", 10);
        payload1.put("meta", Map.of("z", 2, "a", 1));

        Map<String, Object> payload2 = new LinkedHashMap<>();
        payload2.put("meta", Map.of("a", 1, "z", 2));
        payload2.put("userId", 10);
        payload2.put("deviceId", 20);

        String signature = signingService.sign(payload1);

        assertThat(signingService.verify(payload2, signature)).isTrue();
    }

    @Test
    void shouldCanonicalizeJsonAccordingToRfc8785() {
        JsonCanonicalizationService canonicalizationService = new JsonCanonicalizationService();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("zeta", "last");
        payload.put("alpha", 1);
        payload.put("nested", Map.of("b", 2, "a", 1));

        String canonicalJson = new String(canonicalizationService.canonicalize(payload), StandardCharsets.UTF_8);

        assertThat(canonicalJson).isEqualTo("{\"alpha\":1,\"nested\":{\"a\":1,\"b\":2},\"zeta\":\"last\"}");
    }

    @Test
    void shouldCanonicalizeNumbersAndEscapesAccordingToRfc8785() {
        JsonCanonicalizationService canonicalizationService = new JsonCanonicalizationService();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("big", new BigDecimal("1e21"));
        payload.put("fraction", new BigDecimal("1.2300"));
        payload.put("small", new BigDecimal("0.00000012"));
        payload.put("text", "line\n\"quoted\"\\path\t");
        payload.put("zero", new BigDecimal("-0.0"));

        String canonicalJson = new String(canonicalizationService.canonicalize(payload), StandardCharsets.UTF_8);

        assertThat(canonicalJson).isEqualTo(
                "{\"big\":1e+21,\"fraction\":1.23,\"small\":1.2e-7,\"text\":\"line\\n\\\"quoted\\\"\\\\path\\t\",\"zero\":0}"
        );
    }
}
