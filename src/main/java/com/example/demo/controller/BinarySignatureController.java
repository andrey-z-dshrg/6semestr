package com.example.demo.controller;

import com.example.demo.dto.SignatureIdsRequest;
import com.example.demo.service.BinarySignatureExportService;
import com.example.demo.service.BinaryPackage;
import com.example.demo.service.MultipartMixedResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/binary/signatures")
// Это отдельный контроллер задания 5.
// Его задача не управлять сигнатурами, а отдавать клиенту бинарные пакеты manifest.bin + data.bin в формате multipart/mixed.
public class BinarySignatureController {

    private final BinarySignatureExportService binarySignatureExportService;
    private final MultipartMixedResponseFactory multipartMixedResponseFactory;

    public BinarySignatureController(BinarySignatureExportService binarySignatureExportService,
                                     MultipartMixedResponseFactory multipartMixedResponseFactory) {
        this.binarySignatureExportService = binarySignatureExportService;
        this.multipartMixedResponseFactory = multipartMixedResponseFactory;
    }

    @GetMapping("/full")
    // Полная бинарная выгрузка содержит только актуальные записи.
    public ResponseEntity<MultiValueMap<String, Object>> getFull() {
        BinaryPackage binaryPackage = binarySignatureExportService.exportFull();
        return multipartMixedResponseFactory.create(binaryPackage.manifestBytes(), binaryPackage.dataBytes());
    }

    @GetMapping("/increment")
    // Инкрементальная бинарная выгрузка принимает since и включает все записи, изменённые после этого момента, даже DELETED.
    public ResponseEntity<MultiValueMap<String, Object>> getIncrement(@RequestParam LocalDateTime since) {
        BinaryPackage binaryPackage = binarySignatureExportService.exportIncrement(since);
        return multipartMixedResponseFactory.create(binaryPackage.manifestBytes(), binaryPackage.dataBytes());
    }

    @PostMapping("/by-ids")
    // Этот метод нужен, когда клиенту надо точечно скачать только несколько сигнатур по UUID.
    public ResponseEntity<MultiValueMap<String, Object>> getByIds(@Valid @RequestBody SignatureIdsRequest request) {
        BinaryPackage binaryPackage = binarySignatureExportService.exportByIds(request.getIds());
        return multipartMixedResponseFactory.create(binaryPackage.manifestBytes(), binaryPackage.dataBytes());
    }
}
