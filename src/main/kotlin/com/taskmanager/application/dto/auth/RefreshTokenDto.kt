package com.taskmanager.application.dto.auth

import jakarta.validation.constraints.NotBlank

data class RefreshTokenDto(
    @field:NotBlank(message = "Refresh Token é obrigatório")
    val refreshToken: String
)