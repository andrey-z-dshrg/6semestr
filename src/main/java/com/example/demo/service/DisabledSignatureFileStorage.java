package com.example.demo.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(prefix = "storage.minio", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledSignatureFileStorage implements SignatureFileStorage {

    private static final String MESSAGE = "File storage is disabled. Configure MinIO and set storage.minio.enabled=true";

    @Override
    public StoredSignatureFile store(String objectKey,
                                     byte[] content,
                                     String contentType,
                                     String originalFilename) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, MESSAGE);
    }

    @Override
    public PresignedFileUrl createPresignedGetUrl(String objectKey, String originalFilename) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, MESSAGE);
    }
}
