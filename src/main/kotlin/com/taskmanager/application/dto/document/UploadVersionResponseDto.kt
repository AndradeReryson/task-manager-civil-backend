package com.taskmanager.application.dto.document

data class UploadVersionResponseDto(
    val documentId: String,
    val version: DocumentVersionDto
)