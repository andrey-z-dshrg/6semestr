package com.example.demo.model;

// Статус текущей записи сигнатуры.
// ACTIVE - запись участвует в обычной работе системы.
// DELETED - запись не удалена физически, но логически исключена из полной выгрузки.
public enum AntivirusSignatureStatus {
    ACTIVE,
    DELETED
}
