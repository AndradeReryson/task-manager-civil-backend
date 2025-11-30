package com.taskmanager.application.dto.user

import com.taskmanager.demo.model.enums.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UpdateUserDto(
    val fullName: String? = null,
    
    @field:Email(message = "Email inválido")
    val email: String? = null,
    
    val role: UserRole? = null,
    
    val isActive: Boolean? = null,
    
    val newPassword: String? = null // Para troca de senha (opcional)
)