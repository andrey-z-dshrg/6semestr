package com.example.demo.signature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
// Этот класс делает подпись воспроизводимой.
// Если один и тот же объект сериализовать двумя разными способами, подпись уже не совпадёт.
// Поэтому перед подписью мы приводим данные к строгому каноническому JSON-виду.
public class JsonCanonicalizationService {

    // Всё, что по модулю больше этой границы, дальше будет переводиться в научную запись.
    private static final BigDecimal LOWER_EXPONENT_BOUND = new BigDecimal("1e-6");
    // Аналогично, очень большие числа тоже нормализуем через scientific notation.
    private static final BigDecimal UPPER_EXPONENT_BOUND = new BigDecimal("1e21");

    // ObjectMapper используется только как первый шаг:
    // он превращает любой Java-объект в дерево JsonNode, после чего канонизация уже идёт вручную.
    private final ObjectMapper objectMapper;

    public JsonCanonicalizationService() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Главный публичный метод класса.
    // На вход подаётся любой объект, который Jackson умеет читать,
    // а на выходе получается готовый канонический UTF-8 JSON для подписи.
    public byte[] canonicalize(Object payload) {
        try {
            JsonNode node = objectMapper.valueToTree(payload);
            String canonicalJson = toCanonicalJson(node);
            return canonicalJson.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Canonicalization failed: " + e.getMessage(), e);
        }
    }

    // Рекурсивно проходит по JSON-дереву и собирает строку по фиксированным правилам.
    // Для объектов поля сортируются, для массивов порядок сохраняется как есть.
    private String toCanonicalJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return "null";
        }

        if (node.isObject()) {
            List<String> fieldNames = new ArrayList<>();
            node.fieldNames().forEachRemaining(fieldNames::add);
            Collections.sort(fieldNames);

            StringBuilder builder = new StringBuilder("{");
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                if (i > 0) {
                    builder.append(',');
                }
                builder.append(writeJsonString(fieldName))
                        .append(':')
                        .append(toCanonicalJson(node.get(fieldName)));
            }
            return builder.append('}').toString();
        }

        if (node.isArray()) {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < node.size(); i++) {
                if (i > 0) {
                    builder.append(',');
                }
                builder.append(toCanonicalJson(node.get(i)));
            }
            return builder.append(']').toString();
        }

        if (node.isTextual()) {
            return writeJsonString(node.textValue());
        }

        if (node.isBoolean()) {
            return Boolean.toString(node.booleanValue());
        }

        if (node.isNumber()) {
            return canonicalizeNumber(node);
        }

        return node.toString();
    }

    // Переводит строку в безопасный JSON-вид.
    // Все управляющие символы и спецсимволы экранируются строго одинаково.
    private String writeJsonString(String value) {
        StringBuilder builder = new StringBuilder(value.length() + 2);
        builder.append('"');

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (ch <= 0x1F) {
                        builder.append(String.format("\\u%04x", (int) ch));
                    } else {
                        builder.append(ch);
                    }
                }
            }
        }

        return builder.append('"').toString();
    }

    // Числа тоже должны иметь единый текстовый вид.
    // Например, 1.2300 и 1.23 математически равны, но как строки отличаются,
    // поэтому здесь лишние различия убираются.
    private String canonicalizeNumber(JsonNode node) {
        if (node.isIntegralNumber()) {
            return node.bigIntegerValue().toString();
        }

        BigDecimal decimal = node.decimalValue().stripTrailingZeros();
        if (decimal.signum() == 0) {
            return "0";
        }

        BigDecimal absolute = decimal.abs();
        if (absolute.compareTo(LOWER_EXPONENT_BOUND) >= 0
                && absolute.compareTo(UPPER_EXPONENT_BOUND) < 0) {
            return decimal.toPlainString();
        }

        return toScientificNotation(decimal);
    }

    // Приводит число к нормализованной научной записи вроде 1.2e-7 или 1e+21.
    private String toScientificNotation(BigDecimal decimal) {
        BigDecimal normalized = decimal.stripTrailingZeros();
        String digits = normalized.unscaledValue().abs().toString();
        int exponent = -normalized.scale() + digits.length() - 1;

        StringBuilder builder = new StringBuilder();
        if (normalized.signum() < 0) {
            builder.append('-');
        }

        builder.append(digits.charAt(0));
        if (digits.length() > 1) {
            builder.append('.').append(digits.substring(1));
        }

        builder.append('e');
        if (exponent >= 0) {
            builder.append('+');
        }
        builder.append(exponent);

        return builder.toString();
    }
}
