package com.taskmanager.application.dto.document

import java.time.LocalDateTime

data class DocumentVersionDto(
    val id: String,
    val versionNumber: Int,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String?,
    val uploadedByEmployeeId: String,
    val uploadDate: LocalDateTime
)