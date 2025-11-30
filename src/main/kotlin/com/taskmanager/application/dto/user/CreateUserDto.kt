package com.taskmanager.application.dto.user

import com.taskmanager.demo.model.enums.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateUserDto(
    @field:NotBlank(message = "Username é obrigatório")
    @field:Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    val username: String,

    @field:NotBlank(message = "Senha é obrigatória")
    @field:Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    val password: String,

    @field:NotBlank(message = "Nome completo é obrigatório")
    val fullName: String,

    @field:Email(message = "Email inválido")
    val email: String?,

    // A Role deve ser válida conforme o enum UserRole
    val role: UserRole
)