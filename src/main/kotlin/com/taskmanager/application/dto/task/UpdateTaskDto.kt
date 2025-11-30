package com.taskmanager.application.dto.task

import com.taskmanager.demo.model.enums.TaskPriority
import com.taskmanager.demo.model.enums.TaskStatus
import java.time.LocalDateTime

data class UpdateTaskDto(
    val title: String? = null,
    val description: String? = null,
    val projectId: String? = null,
    val assignedToId: String? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val dueDate: LocalDateTime? = null,
    val completionDate: LocalDateTime? = null,
    val isActive: Boolean? = null
)