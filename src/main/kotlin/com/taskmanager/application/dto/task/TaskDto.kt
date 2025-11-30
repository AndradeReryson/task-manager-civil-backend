package com.taskmanager.application.dto.task

import com.taskmanager.demo.model.enums.TaskPriority
import com.taskmanager.demo.model.enums.TaskStatus
import java.time.LocalDateTime

data class TaskDto(
    val id: String,
    val title: String,
    val description: String?,
    val projectId: String,
    val assignedToId: String?, // ID do Employee responsável
    val reporterId: String?,   // ID do Employee que reportou/criou a tarefa
    val status: TaskStatus,
    val priority: TaskPriority,
    val dueDate: LocalDateTime?,
    val completionDate: LocalDateTime?,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)