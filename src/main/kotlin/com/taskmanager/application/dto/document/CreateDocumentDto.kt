package com.taskmanager.application.dto.document

import com.taskmanager.demo.model.enums.DocumentType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateDocumentDto(
    @field:NotBlank(message = "Título do documento é obrigatório")
    @field:Size(max = 255)
    val title: String,

    val type: DocumentType,

    @field:NotBlank(message = "ID do Projeto é obrigatório")
    val projectId: String,

    val taskId: String? = null // Opcional, se o documento estiver ligado a uma tarefa
)