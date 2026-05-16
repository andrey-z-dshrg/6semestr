package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SignatureFileUrlResponse(
        UUID signatureId,
        String originalFilename,
        String storageBucket,
        String storageObjectKey,
        String presignedUrl,
        LocalDateTime expiresAt
) {
}
