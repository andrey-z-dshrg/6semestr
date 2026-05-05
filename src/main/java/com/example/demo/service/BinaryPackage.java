package com.example.demo.service;

// Это простой контейнер с двумя бинарными частями ответа.
// Одна часть содержит manifest.bin, вторая data.bin.
public record BinaryPackage(byte[] manifestBytes, byte[] dataBytes) {
}
