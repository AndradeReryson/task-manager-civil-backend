package com.taskmanager.application.dto.financial

import com.taskmanager.demo.model.enums.FinancialCategory
import com.taskmanager.demo.model.enums.FinancialType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class FinancialDto(
    val id: String,
    val description: String,
    val type: FinancialType,
    val category: FinancialCategory,
    val amount: BigDecimal,
    val transactionDate: LocalDate,
    val projectId: String?,
    val employeeId: String?,
    val receiptUrl: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)