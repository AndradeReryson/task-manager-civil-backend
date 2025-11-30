package com.taskmanager.application.dto.auth

data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer", // Padrão JWT
    val expiresIn: Long // Tempo de expiração do accessToken em segundos
)