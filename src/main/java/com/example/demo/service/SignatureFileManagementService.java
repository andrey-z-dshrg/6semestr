package com.example.demo.service;

import com.example.demo.config.MinioStorageProperties;
import com.example.demo.dto.AntivirusSignatureCreateRequest;
import com.example.demo.dto.AntivirusSignatureResponse;
import com.example.demo.dto.SignatureFileUploadResponse;
import com.example.demo.dto.SignatureFileUrlResponse;
import com.example.demo.model.AntivirusSignature;
import com.example.demo.repository.AntivirusSignatureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SignatureFileManagementService {

    private static final HexFormat HEX = HexFormat.of().withUpperCase();

    private final AntivirusSignatureService antivirusSignatureService;
    private final AntivirusSignatureRepository signatureRepository;
    private final SignatureFileStorage signatureFileStorage;
    private final MinioStorageProperties storageProperties;

    public SignatureFileManagementService(AntivirusSignatureService antivirusSignatureService,
                                          AntivirusSignatureRepository signatureRepository,
                                          SignatureFileStorage signatureFileStorage,
                                          MinioStorageProperties storageProperties) {
        this.antivirusSignatureService = antivirusSignatureService;
        this.signatureRepository = signatureRepository;
        this.signatureFileStorage = signatureFileStorage;
        this.storageProperties = storageProperties;
    }

    @Transactional
    public SignatureFileUploadResponse upload(MultipartFile file, String threatName, String actor) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must not be empty");
        }

        byte[] content = readContent(file);
        String resolvedThreatName = safeThreatName(threatName, file.getOriginalFilename());
        ComputedSignatureFields computed = computeFields(content, resolvedThreatName, file.getOriginalFilename());

        AntivirusSignatureCreateRequest request = new AntivirusSignatureCreateRequest();
        request.setThreatName(computed.threatName());
        request.setFirstBytesHex(computed.firstBytesHex());
        request.setRemainderHashHex(computed.remainderHashHex());
        request.setRemainderLength(computed.remainderLength());
        request.setFileType(computed.fileType());
        request.setOffsetStart(0L);
        request.setOffsetEnd(computed.offsetEnd());

        AntivirusSignatureResponse created = antivirusSignatureService.create(request, actor);

        String objectKey = buildObjectKey(created.id(), file.getOriginalFilename());
        SignatureFileStorage.StoredSignatureFile stored = signatureFileStorage.store(
                objectKey,
                content,
                normalizeContentType(file.getContentType()),
                fallbackFilename(file.getOriginalFilename())
        );

        AntivirusSignature signature = signatureRepository.findById(created.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found"));
        signature.setSourceBucket(stored.bucket());
        signature.setSourceObjectKey(stored.objectKey());
        signature.setSourceOriginalFilename(stored.originalFilename());
        signature.setSourceContentType(stored.contentType());
        signature.setSourceSizeBytes(stored.sizeBytes());
        signatureRepository.save(signature);

        return new SignatureFileUploadResponse(
                signature.getId(),
                signature.getThreatName(),
                signature.getFileType(),
                signature.getFirstBytesHex(),
                signature.getRemainderHashHex(),
                signature.getRemainderLength(),
                signature.getOffsetStart(),
                signature.getOffsetEnd(),
                signature.getStatus(),
                signature.getUpdatedAt(),
                signature.getSourceOriginalFilename(),
                signature.getSourceBucket(),
                signature.getSourceObjectKey(),
                signature.getSourceSizeBytes(),
                signature.getDigitalSignatureBase64()
        );
    }

    @Transactional(readOnly = true)
    public List<SignatureFileUrlResponse> getPresignedUrlsByIds(List<UUID> ids) {
        Map<UUID, Integer> positions = new LinkedHashMap<>();
        for (int index = 0; index < ids.size(); index++) {
            positions.putIfAbsent(ids.get(index), index);
        }

        List<AntivirusSignature> signatures = signatureRepository.findAllById(ids).stream()
                .sorted((left, right) -> Integer.compare(
                        positions.getOrDefault(left.getId(), Integer.MAX_VALUE),
                        positions.getOrDefault(right.getId(), Integer.MAX_VALUE)))
                .toList();

        if (signatures.size() != positions.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more signatures were not found");
        }

        List<SignatureFileUrlResponse> response = new ArrayList<>();
        for (AntivirusSignature signature : signatures) {
            if (signature.getSourceObjectKey() == null || signature.getSourceOriginalFilename() == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Signature " + signature.getId() + " does not have a source file in storage"
                );
            }
            SignatureFileStorage.PresignedFileUrl presigned = signatureFileStorage.createPresignedGetUrl(
                    signature.getSourceObjectKey(),
                    signature.getSourceOriginalFilename()
            );
            response.add(new SignatureFileUrlResponse(
                    signature.getId(),
                    presigned.originalFilename(),
                    presigned.bucket(),
                    presigned.objectKey(),
                    presigned.url(),
                    presigned.expiresAt()
            ));
        }
        return response;
    }

    private ComputedSignatureFields computeFields(byte[] content, String threatName, String originalFilename) {
        int firstBytesLength = Math.max(1, storageProperties.getFirstBytesLength());
        int prefixLength = Math.min(firstBytesLength, content.length);
        byte[] prefix = new byte[prefixLength];
        byte[] remainder = new byte[content.length - prefixLength];

        System.arraycopy(content, 0, prefix, 0, prefixLength);
        System.arraycopy(content, prefixLength, remainder, 0, remainder.length);

        return new ComputedSignatureFields(
                threatName,
                HEX.formatHex(prefix),
                HEX.formatHex(sha256(remainder)),
                (long) remainder.length,
                detectFileType(originalFilename),
                prefixLength - 1L
        );
    }

    private String detectFileType(String threatNameOrFilename) {
        String filename = threatNameOrFilename == null ? "" : threatNameOrFilename;
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "bin";
    }

    private String safeThreatName(String threatName, String originalFilename) {
        if (threatName != null && !threatName.isBlank()) {
            return threatName.trim();
        }
        String filename = fallbackFilename(originalFilename);
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }

    private String fallbackFilename(String originalFilename) {
        return originalFilename == null || originalFilename.isBlank() ? "signature.bin" : originalFilename;
    }

    private String normalizeContentType(String contentType) {
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }

    private String buildObjectKey(UUID signatureId, String originalFilename) {
        String sanitizedFilename = fallbackFilename(originalFilename).replace("\\", "_").replace("/", "_").replace(" ", "_");
        return "signatures/" + signatureId + "/" + sanitizedFilename;
    }

    private byte[] readContent(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot read uploaded file", e);
        }
    }

    private byte[] sha256(byte[] content) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(content);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private record ComputedSignatureFields(
            String threatName,
            String firstBytesHex,
            String remainderHashHex,
            Long remainderLength,
            String fileType,
            Long offsetEnd
    ) {
    }
}
