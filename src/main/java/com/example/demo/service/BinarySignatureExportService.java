package com.example.demo.service;

import com.example.demo.model.AntivirusSignature;
import com.example.demo.model.AntivirusSignatureStatus;
import com.example.demo.signature.SigningService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
// Этот сервис отвечает только за транспортный бинарный формат из задания 5.
// Он не меняет бизнес-логику сигнатур, а берёт уже готовые записи и превращает их в manifest.bin и data.bin.
public class BinarySignatureExportService {

    private static final int FORMAT_VERSION = 1;
    private static final int EXPORT_TYPE_FULL = 1;
    private static final int EXPORT_TYPE_INCREMENT = 2;
    private static final int EXPORT_TYPE_BY_IDS = 3;
    private static final String STUDENT_SURNAME = "Makarov";

    private final AntivirusSignatureService antivirusSignatureService;
    private final SigningService signingService;

    public BinarySignatureExportService(AntivirusSignatureService antivirusSignatureService,
                                        SigningService signingService) {
        this.antivirusSignatureService = antivirusSignatureService;
        this.signingService = signingService;
    }

    public BinaryPackage exportFull() {
        List<AntivirusSignature> signatures = antivirusSignatureService.getFullExportEntities();
        return buildPackage(signatures, EXPORT_TYPE_FULL, null);
    }

    public BinaryPackage exportIncrement(LocalDateTime since) {
        List<AntivirusSignature> signatures = antivirusSignatureService.getIncrementEntities(since);
        return buildPackage(signatures, EXPORT_TYPE_INCREMENT, since);
    }

    public BinaryPackage exportByIds(List<UUID> ids) {
        List<AntivirusSignature> signatures = antivirusSignatureService.getByIdsEntities(ids);
        return buildPackage(signatures, EXPORT_TYPE_BY_IDS, null);
    }

    private BinaryPackage buildPackage(List<AntivirusSignature> signatures, int exportType, LocalDateTime since) {
        List<byte[]> recordBytes = signatures.stream()
                .map(this::serializeDataRecord)
                .toList();

        byte[] dataBytes = buildDataBytes(recordBytes);
        byte[] manifestBytes = buildManifestBytes(signatures, recordBytes, dataBytes, exportType, since);
        return new BinaryPackage(manifestBytes, dataBytes);
    }

    private byte[] buildDataBytes(List<byte[]> recordBytes) {
        BinaryProtocolWriter writer = new BinaryProtocolWriter();
        writer.writeUtf8("DB-" + STUDENT_SURNAME);
        writer.writeUInt16(FORMAT_VERSION);
        writer.writeUInt32(recordBytes.size());

        for (byte[] record : recordBytes) {
            writer.writeRawBytes(record);
        }
        return writer.toByteArray();
    }

    private byte[] buildManifestBytes(List<AntivirusSignature> signatures,
                                      List<byte[]> recordBytes,
                                      byte[] dataBytes,
                                      int exportType,
                                      LocalDateTime since) {
        BinaryProtocolWriter manifestWriter = new BinaryProtocolWriter();
        manifestWriter.writeUtf8("MF-" + STUDENT_SURNAME);
        manifestWriter.writeUInt16(FORMAT_VERSION);
        manifestWriter.writeUInt8(exportType);
        manifestWriter.writeInt64(toEpochMillis(LocalDateTime.now().withNano(0)));
        manifestWriter.writeInt64(since == null ? -1L : toEpochMillis(since));
        manifestWriter.writeUInt32(signatures.size());
        manifestWriter.writeRawBytes(sha256(dataBytes));

        long offset = 0;
        for (int i = 0; i < signatures.size(); i++) {
            AntivirusSignature signature = signatures.get(i);
            byte[] record = recordBytes.get(i);
            byte[] signatureBytes = Base64.getDecoder().decode(signature.getDigitalSignatureBase64());

            manifestWriter.writeUuid(signature.getId());
            manifestWriter.writeUInt8(statusCode(signature.getStatus()));
            manifestWriter.writeInt64(toEpochMillis(signature.getUpdatedAt()));
            manifestWriter.writeInt64(offset);
            manifestWriter.writeUInt32(record.length);
            manifestWriter.writeUInt32(signatureBytes.length);
            manifestWriter.writeRawBytes(signatureBytes);

            offset += record.length;
        }

        byte[] unsignedManifest = manifestWriter.toByteArray();
        byte[] manifestSignature = signingService.sign(unsignedManifest);

        BinaryProtocolWriter signedManifestWriter = new BinaryProtocolWriter();
        signedManifestWriter.writeRawBytes(unsignedManifest);
        signedManifestWriter.writeUInt32(manifestSignature.length);
        signedManifestWriter.writeRawBytes(manifestSignature);
        return signedManifestWriter.toByteArray();
    }

    private byte[] serializeDataRecord(AntivirusSignature signature) {
        BinaryProtocolWriter writer = new BinaryProtocolWriter();
        writer.writeUtf8(signature.getThreatName());
        writer.writeByteArray(hexToBytes(signature.getFirstBytesHex()));
        writer.writeByteArray(hexToBytes(signature.getRemainderHashHex()));
        writer.writeInt64(signature.getRemainderLength());
        writer.writeUtf8(signature.getFileType());
        writer.writeInt64(signature.getOffsetStart());
        writer.writeInt64(signature.getOffsetEnd());
        return writer.toByteArray();
    }

    private int statusCode(AntivirusSignatureStatus status) {
        return status == AntivirusSignatureStatus.ACTUAL ? 1 : 2;
    }

    private long toEpochMillis(LocalDateTime value) {
        return value.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private byte[] sha256(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private byte[] hexToBytes(String hex) {
        String normalized = hex.trim();
        if ((normalized.length() & 1) != 0) {
            normalized = "0" + normalized;
        }

        byte[] bytes = new byte[normalized.length() / 2];
        for (int i = 0; i < normalized.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(normalized.substring(i, i + 2), 16);
        }
        return bytes;
    }
}
