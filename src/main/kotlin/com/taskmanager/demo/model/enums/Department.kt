package com.taskmanager.demo.model.enums

// Estes valores devem ser os mesmos usados nos filtros de API (GET /api/employees?department=...)
enum class Department {
    ENGENHARIA,
    COMPRAS,
    FINANCEIRO,
    RH,
    OPERACIONAL,
    ADMINISTRATIVO
}