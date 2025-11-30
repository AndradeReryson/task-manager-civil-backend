package com.taskmanager.application.dto.project

import com.taskmanager.demo.model.enums.ProjectStatus
import com.taskmanager.application.dto.employee.EmployeeDto // Para referenciar o gestor
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class ProjectDto(
    val id: String,
    val name: String,
    val description: String?,
    val status: ProjectStatus,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val actualEndDate: LocalDate?,
    val budget: BigDecimal?,
    val managerId: String?, // Apenas o ID do manager
    val isActive: Boolean,
    val createdAt: LocalDateTime
    // Relacionamentos com Teams, Tasks e Documents serão listados em endpoints separados ou DTOs detalhados.
)