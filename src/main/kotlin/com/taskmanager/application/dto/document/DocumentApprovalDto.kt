package com.taskmanager.application.dto.document

import com.taskmanager.demo.model.enums.ApprovalStatus
import java.time.LocalDateTime

data class DocumentApprovalDto(
    val id: String,
    val approverEmployeeId: String,
    val requiredRole: String,
    val status: ApprovalStatus,
    val comments: String?,
    val approvalDate: LocalDateTime?
)