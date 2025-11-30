package com.taskmanager.application.dto.team

import jakarta.validation.constraints.Size

data class UpdateTeamDto(
    val name: String? = null,
    val description: String? = null,

    // IDs para atualização
    val leaderId: String? = null,
    
    // Lista completa de IDs de membros. 
    // Em uma API RESTful, PUT/PATCH de relacionamentos M:M é complexo. 
    // Usaremos esta lista como o NOVO estado completo dos membros.
    val memberIds: List<String>? = null, 
    
    val isActive: Boolean? = null
)