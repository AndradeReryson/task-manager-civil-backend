package com.taskmanager.application.dto.employee

import com.taskmanager.demo.model.enums.Department
import com.taskmanager.demo.model.enums.EmployeeStatus
import com.taskmanager.demo.model.enums.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class CreateEmployeeDto(
    @field:NotBlank(message = "Username é obrigatório")
    val username: String,

    @field:NotBlank(message = "Senha é obrigatória")
    @field:Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    val password: String,

    @field:NotBlank(message = "Nome completo é obrigatório")
    val fullName: String,

    @field:Email(message = "Email inválido")
    val email: String?,

    // Informações de Employee
    @field:NotBlank(message = "Número de registro é obrigatório")
    val registrationNumber: String,

    val role: UserRole, // Role do usuário associado

    val department: Department,
    
    val status: EmployeeStatus = EmployeeStatus.ACTIVE,

    val phone: String?,

    val hireDate: LocalDate?,
    
    // O salário e outras informações sensíveis podem ser adicionados em um update ou endpoint separado, 
    // mas incluiremos aqui para simplicidade.
    // val salary: BigDecimal? 
)