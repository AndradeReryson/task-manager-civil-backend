package com.taskmanager.application.dto.team

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTeamDto(
    @field:NotBlank(message = "Nome da equipe é obrigatório")
    @field:Size(max = 255)
    val name: String,

    val description: String?,

    // IDs opcionais para o líder e membros iniciais
    val leaderId: String? = null,
    val memberIds: List<String> = emptyList()
)