package com.taskmanager.application.dto.financial

import com.taskmanager.demo.model.enums.FinancialCategory
import com.taskmanager.demo.model.enums.FinancialType
import java.math.BigDecimal
import java.time.LocalDate

data class UpdateFinancialDto(
    val description: String? = null,
    val type: FinancialType? = null,
    val category: FinancialCategory? = null,
    val amount: BigDecimal? = null,
    val transactionDate: LocalDate? = null,
    val projectId: String? = null,
    val employeeId: String? = null,
    val receiptUrl: String? = null,
    val isActive: Boolean? = null
)