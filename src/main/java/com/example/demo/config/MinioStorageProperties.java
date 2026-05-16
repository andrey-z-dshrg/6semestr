package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.minio")
public class MinioStorageProperties {

    private boolean enabled;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket = "signature-files";
    private int presignedUrlTtlMinutes = 30;
    private int firstBytesLength = 16;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public int getPresignedUrlTtlMinutes() {
        return presignedUrlTtlMinutes;
    }

    public void setPresignedUrlTtlMinutes(int presignedUrlTtlMinutes) {
        this.presignedUrlTtlMinutes = presignedUrlTtlMinutes;
    }

    public int getFirstBytesLength() {
        return firstBytesLength;
    }

    public void setFirstBytesLength(int firstBytesLength) {
        this.firstBytesLength = firstBytesLength;
    }
}
