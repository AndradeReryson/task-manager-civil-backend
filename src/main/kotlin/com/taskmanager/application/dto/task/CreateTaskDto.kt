package com.taskmanager.application.dto.task

import com.taskmanager.demo.model.enums.TaskPriority
import com.taskmanager.demo.model.enums.TaskStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateTaskDto(
    @field:NotBlank(message = "Título da tarefa é obrigatório")
    @field:Size(min = 3, max = 200)
    val title: String,

    @field:Size(min = 10)
    val description: String?,

    @field:NotBlank(message = "ID do Projeto é obrigatório")
    val projectId: String,

    val assignedToId: String?,

    // O reporterId será injetado pelo Controller com base no usuário logado
    
    // Status inicial padrão
    val status: TaskStatus = TaskStatus.PENDENTE, 

    val priority: TaskPriority = TaskPriority.MEDIA,
    
    val dueDate: LocalDateTime? = null
)