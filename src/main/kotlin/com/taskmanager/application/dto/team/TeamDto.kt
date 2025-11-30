package com.taskmanager.application.dto.team

import com.taskmanager.application.dto.employee.EmployeeDto // Para o líder
import java.time.LocalDateTime

data class TeamDto(
    val id: String,
    val name: String,
    val description: String?,
    val leaderId: String?, // ID do Employee que é o líder da equipe
    val memberIds: List<String>, // IDs dos Employees que são membros
    val isActive: Boolean,
    val createdAt: LocalDateTime
    // IDs dos projetos associados podem ser carregados em endpoints separados
)