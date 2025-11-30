package com.taskmanager.application.dto.user

import com.taskmanager.demo.model.enums.UserRole // Importando o Enum de Role
import java.time.LocalDateTime

data class UserDto(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String?,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)