package com.taskmanager.application.dto.document

import com.taskmanager.demo.model.enums.DocumentStatus
import com.taskmanager.demo.model.enums.DocumentType
import java.time.LocalDateTime

data class DocumentDto(
    val id: String,
    val title: String,
    val type: DocumentType,
    val status: DocumentStatus,
    val projectId: String,
    val taskId: String?,
    val latestVersionNumber: Int?, // Número da versão mais recente
    val isActive: Boolean,
    val createdAt: LocalDateTime
)