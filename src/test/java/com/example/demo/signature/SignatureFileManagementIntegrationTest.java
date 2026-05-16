package com.example.demo.signature;

import com.example.demo.dto.SignatureIdsRequest;
import com.example.demo.repository.AntivirusSignatureAuditRepository;
import com.example.demo.repository.AntivirusSignatureHistoryRepository;
import com.example.demo.repository.AntivirusSignatureRepository;
import com.example.demo.service.SignatureFileStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignatureFileManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AntivirusSignatureRepository signatureRepository;

    @Autowired
    private AntivirusSignatureHistoryRepository historyRepository;

    @Autowired
    private AntivirusSignatureAuditRepository auditRepository;

    @BeforeEach
    void setUp() {
        auditRepository.deleteAll();
        historyRepository.deleteAll();
        signatureRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin1", roles = "ADMIN")
    void shouldUploadSignatureFileAndReturnMetadata() throws Exception {
        byte[] content = "MZ-test-payload-for-task-6".getBytes(StandardCharsets.UTF_8);

        String body = mockMvc.perform(
                        multipart("/api/signatures/upload")
                                .file("file", content)
                                .param("threatName", "Trojan.Test.Upload")
                                .with(csrf())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.threatName").value("Trojan.Test.Upload"))
                .andExpect(jsonPath("$.storageBucket").value("test-signature-files"))
                .andExpect(jsonPath("$.originalFilename").value("signature.bin"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<?, ?> parsed = objectMapper.readValue(body, Map.class);
        String id = parsed.get("id").toString();

        assertThat(signatureRepository.findById(java.util.UUID.fromString(id))).isPresent();
        assertThat(signatureRepository.findById(java.util.UUID.fromString(id)).orElseThrow().getSourceObjectKey())
                .contains(id);
    }

    @Test
    @WithMockUser(username = "admin1", roles = "ADMIN")
    void shouldReturnPresignedUrlsByIds() throws Exception {
        byte[] content = "MZ-another-test-payload".getBytes(StandardCharsets.UTF_8);

        String uploadResponse = mockMvc.perform(
                        multipart("/api/signatures/upload")
                                .file("file", "sample.exe", "application/octet-stream", content)
                                .param("threatName", "Trojan.Test.Url")
                                .with(csrf())
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(uploadResponse).get("id").asText();
        SignatureIdsRequest request = new SignatureIdsRequest();
        request.setIds(List.of(java.util.UUID.fromString(id)));

        mockMvc.perform(
                        post("/api/signatures/files/presigned-urls/by-ids")
                                .with(csrf())
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsBytes(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].signatureId").value(id))
                .andExpect(jsonPath("$[0].storageBucket").value("test-signature-files"))
                .andExpect(jsonPath("$[0].presignedUrl").value(org.hamcrest.Matchers.containsString("https://example.test/download/")));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void shouldForbidUploadForNonAdmin() throws Exception {
        mockMvc.perform(
                        multipart("/api/signatures/upload")
                                .file("file", "payload".getBytes(StandardCharsets.UTF_8))
                                .with(csrf())
                )
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    static class TestStorageConfiguration {

        @Bean
        @Primary
        SignatureFileStorage signatureFileStorage() {
            return new InMemorySignatureFileStorage();
        }
    }

    static class InMemorySignatureFileStorage implements SignatureFileStorage {

        private final Map<String, byte[]> storage = new ConcurrentHashMap<>();

        @Override
        public StoredSignatureFile store(String objectKey, byte[] content, String contentType, String originalFilename) {
            storage.put(objectKey, content.clone());
            return new StoredSignatureFile("test-signature-files", objectKey, originalFilename, contentType, content.length);
        }

        @Override
        public PresignedFileUrl createPresignedGetUrl(String objectKey, String originalFilename) {
            if (!storage.containsKey(objectKey)) {
                throw new IllegalStateException("Stored object not found in test storage");
            }
            return new PresignedFileUrl(
                    "test-signature-files",
                    objectKey,
                    originalFilename,
                    "https://example.test/download/" + objectKey,
                    LocalDateTime.of(2026, 5, 16, 12, 0)
            );
        }
    }
}
