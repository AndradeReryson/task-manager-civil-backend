package com.taskmanager.application.dto.auth

import jakarta.validation.constraints.NotBlank

data class LoginRequestDto(
    @field:NotBlank(message = "Username é obrigatório")
    val username: String,
    
    @field:NotBlank(message = "Senha é obrigatória")
    val password: String
)