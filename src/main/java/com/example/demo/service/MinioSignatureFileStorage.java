package com.example.demo.service;

import com.example.demo.config.MinioStorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

@Service
@ConditionalOnProperty(prefix = "storage.minio", name = "enabled", havingValue = "true")
public class MinioSignatureFileStorage implements SignatureFileStorage {

    private final MinioStorageProperties properties;
    private final MinioClient minioClient;

    public MinioSignatureFileStorage(MinioStorageProperties properties) {
        this.properties = properties;
        this.minioClient = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    @PostConstruct
    void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getBucket()).build()
            );
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize MinIO bucket", e);
        }
    }

    @Override
    public StoredSignatureFile store(String objectKey,
                                     byte[] content,
                                     String contentType,
                                     String originalFilename) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectKey)
                            .stream(new ByteArrayInputStream(content), content.length, -1)
                            .contentType(contentType)
                            .build()
            );
            return new StoredSignatureFile(
                    properties.getBucket(),
                    objectKey,
                    originalFilename,
                    contentType,
                    content.length
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cannot store signature source file", e);
        }
    }

    @Override
    public PresignedFileUrl createPresignedGetUrl(String objectKey, String originalFilename) {
        try {
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(properties.getPresignedUrlTtlMinutes());
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucket())
                            .object(objectKey)
                            .expiry(properties.getPresignedUrlTtlMinutes() * 60)
                            .build()
            );
            return new PresignedFileUrl(
                    properties.getBucket(),
                    objectKey,
                    originalFilename,
                    url,
                    expiresAt.withNano(0)
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cannot create pre-signed URL", e);
        }
    }
}
