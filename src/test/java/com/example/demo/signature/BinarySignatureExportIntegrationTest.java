package com.example.demo.signature;

import com.example.demo.dto.AntivirusSignatureCreateRequest;
import com.example.demo.service.AntivirusSignatureService;
import com.example.demo.service.BinaryPackage;
import com.example.demo.service.BinarySignatureExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
// Этот тест уже проверяет само задание 5:
// сборку бинарного пакета, наличие подписи манифеста и multipart/mixed ответ контроллера.
class BinarySignatureExportIntegrationTest {

    @Autowired
    private AntivirusSignatureService antivirusSignatureService;

    @Autowired
    private BinarySignatureExportService binarySignatureExportService;

    @Autowired
    private SigningService signingService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.example.demo.repository.AntivirusSignatureAuditRepository auditRepository;

    @Autowired
    private com.example.demo.repository.AntivirusSignatureHistoryRepository historyRepository;

    @Autowired
    private com.example.demo.repository.AntivirusSignatureRepository signatureRepository;

    @BeforeEach
    void setUp() {
        auditRepository.deleteAll();
        historyRepository.deleteAll();
        signatureRepository.deleteAll();

        AntivirusSignatureCreateRequest createRequest = new AntivirusSignatureCreateRequest();
        createRequest.setThreatName("Worm.Script.Test");
        createRequest.setFirstBytesHex("AABBCCDD");
        createRequest.setRemainderHashHex("11223344556677889900AABBCCDDEEFF");
        createRequest.setRemainderLength(12L);
        createRequest.setFileType("js");
        createRequest.setOffsetStart(10L);
        createRequest.setOffsetEnd(30L);
        antivirusSignatureService.create(createRequest, "admin1");
    }

    @Test
    void shouldBuildSignedBinaryPackage() throws Exception {
        BinaryPackage binaryPackage = binarySignatureExportService.exportFull();

        String manifestAsIso = new String(binaryPackage.manifestBytes(), StandardCharsets.ISO_8859_1);
        String dataAsIso = new String(binaryPackage.dataBytes(), StandardCharsets.ISO_8859_1);

        assertThat(manifestAsIso).contains("MF-Makarov");
        assertThat(dataAsIso).contains("DB-Makarov");
        assertThat(dataAsIso).contains("Worm.Script.Test");

        ManifestParseResult parsedManifest = parseManifest(binaryPackage.manifestBytes());
        assertThat(parsedManifest.recordCount()).isEqualTo(1);
        assertThat(parsedManifest.exportType()).isEqualTo(1);
        assertThat(signingService.verify(parsedManifest.unsignedManifest(), parsedManifest.manifestSignature())).isTrue();
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void shouldReturnMultipartMixedFromBinaryController() throws Exception {
        mockMvc.perform(get("/api/binary/signatures/full"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("multipart/mixed"))
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.ISO_8859_1);
                    assertThat(response).contains("filename=\"manifest.bin\"");
                    assertThat(response).contains("filename=\"data.bin\"");
                });
    }

    private ManifestParseResult parseManifest(byte[] manifestBytes) throws Exception {
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(manifestBytes));

        int magicLength = input.readInt();
        byte[] magicBytes = input.readNBytes(magicLength);
        input.readUnsignedShort();
        int exportType = input.readUnsignedByte();
        input.readLong();
        input.readLong();
        long recordCount = Integer.toUnsignedLong(input.readInt());
        input.readNBytes(32);

        for (int i = 0; i < recordCount; i++) {
            input.readLong();
            input.readLong();
            input.readUnsignedByte();
            input.readLong();
            input.readLong();
            input.readInt();
            int recordSignatureLength = input.readInt();
            input.readNBytes(recordSignatureLength);
        }

        int unsignedLength = manifestBytes.length - input.available();
        int manifestSignatureLength = input.readInt();
        byte[] manifestSignature = input.readNBytes(manifestSignatureLength);
        byte[] unsignedManifest = Arrays.copyOf(manifestBytes, unsignedLength);

        assertThat(new String(magicBytes, StandardCharsets.UTF_8)).isEqualTo("MF-Makarov");
        return new ManifestParseResult(exportType, recordCount, unsignedManifest, manifestSignature);
    }

    private record ManifestParseResult(int exportType,
                                       long recordCount,
                                       byte[] unsignedManifest,
                                       byte[] manifestSignature) {
    }
}
