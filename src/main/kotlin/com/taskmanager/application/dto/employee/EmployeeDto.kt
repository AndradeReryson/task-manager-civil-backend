package com.taskmanager.application.dto.employee

import com.taskmanager.demo.model.enums.Department
import com.taskmanager.demo.model.enums.EmployeeStatus
import com.taskmanager.demo.model.enums.UserRole
import com.taskmanager.application.dto.user.UserDto // Para embeddar a informação do usuário
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class EmployeeDto(
    val id: String,
    val registrationNumber: String, // Matrícula
    val fullName: String,           // Nome completo (do User)
    val email: String?,             // Email (do User)
    val role: UserRole,             // Role (do User)
    val department: Department,
    val status: EmployeeStatus,
    val phone: String?,
    val hireDate: LocalDate?,
    val salary: BigDecimal?,
    val isActive: Boolean,
    val createdAt: LocalDateTime
    // Não incluímos o relacionamento com Teams aqui para simplificar a listagem
)