package com.taskmanager.application.dto.employee

import com.taskmanager.demo.model.enums.Department
import com.taskmanager.demo.model.enums.EmployeeStatus
import com.taskmanager.demo.model.enums.UserRole
import jakarta.validation.constraints.Email
import java.math.BigDecimal

data class UpdateEmployeeDto(
    // Campos que podem ser atualizados no User
    val fullName: String? = null,
    
    @field:Email(message = "Email inválido")
    val email: String? = null,
    
    val role: UserRole? = null,
    
    // Campos que podem ser atualizados no Employee
    val registrationNumber: String? = null,
    val department: Department? = null,
    val status: EmployeeStatus? = null,
    val phone: String? = null,
    val salary: BigDecimal? = null,
    
    val isActive: Boolean? = null // Para soft delete/ativação manual
)