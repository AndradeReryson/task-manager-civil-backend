package com.taskmanager.application.dto.document

import com.taskmanager.demo.model.enums.DocumentType
import com.taskmanager.demo.model.enums.DocumentStatus

data class UpdateDocumentDto(
    val title: String? = null,
    val type: DocumentType? = null,
    val status: DocumentStatus? = null, // Para alterar o status de aprovação/revisão
    val taskId: String? = null,
    val isActive: Boolean? = null
)