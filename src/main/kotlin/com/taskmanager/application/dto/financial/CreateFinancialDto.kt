package com.taskmanager.application.dto.financial

import com.taskmanager.demo.model.enums.FinancialCategory
import com.taskmanager.demo.model.enums.FinancialType
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate

data class CreateFinancialDto(
    @field:NotBlank(message = "Descrição é obrigatória")
    val description: String,

    @field:NotNull(message = "Tipo de transação é obrigatório")
    val type: FinancialType,

    @field:NotNull(message = "Categoria é obrigatória")
    val category: FinancialCategory,

    @field:NotNull(message = "Valor é obrigatório")
    @field:DecimalMin(value = "0.01", message = "O valor deve ser positivo")
    val amount: BigDecimal,

    @field:NotNull(message = "Data da transação é obrigatória")
    val transactionDate: LocalDate,

    val projectId: String? = null,
    
    val employeeId: String? = null,

    val receiptUrl: String? = null
)