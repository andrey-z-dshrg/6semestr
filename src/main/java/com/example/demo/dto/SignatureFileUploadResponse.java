package com.example.demo.dto;

import com.example.demo.model.AntivirusSignatureStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SignatureFileUploadResponse(
        UUID id,
        String threatName,
        String fileType,
        String firstBytesHex,
        String remainderHashHex,
        Long remainderLength,
        Long offsetStart,
        Long offsetEnd,
        AntivirusSignatureStatus status,
        LocalDateTime updatedAt,
        String originalFilename,
        String storageBucket,
        String storageObjectKey,
        Long sourceSizeBytes,
        String digitalSignatureBase64
) {
}
