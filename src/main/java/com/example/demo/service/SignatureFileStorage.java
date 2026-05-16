package com.example.demo.service;

import java.time.LocalDateTime;

public interface SignatureFileStorage {

    StoredSignatureFile store(String objectKey,
                              byte[] content,
                              String contentType,
                              String originalFilename);

    PresignedFileUrl createPresignedGetUrl(String objectKey, String originalFilename);

    record StoredSignatureFile(
            String bucket,
            String objectKey,
            String originalFilename,
            String contentType,
            long sizeBytes
    ) {
    }

    record PresignedFileUrl(
            String bucket,
            String objectKey,
            String originalFilename,
            String url,
            LocalDateTime expiresAt
    ) {
    }
}
