package com.taskmanager.application.dto.document

import com.taskmanager.demo.model.enums.ApprovalStatus
import jakarta.validation.constraints.NotBlank

data class CreateApprovalRequestDto(
    @field:NotBlank(message = "Status da aprovação é obrigatório")
    val status: ApprovalStatus, // APROVADO ou REJEITADO
    
    val comments: String? = null
    
    // O ID do documento e do funcionário logado são injetados pelo Controller
)