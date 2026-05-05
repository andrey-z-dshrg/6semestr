package com.example.demo.model;

// У сигнатуры в этом проекте только два рабочих состояния.
// ACTUAL означает, что запись активна и участвует в обычной выдаче.
// DELETED означает логическое удаление: строка не исчезает из базы, но в полной выгрузке её уже быть не должно.
public enum AntivirusSignatureStatus {
    ACTUAL,
    DELETED
}
