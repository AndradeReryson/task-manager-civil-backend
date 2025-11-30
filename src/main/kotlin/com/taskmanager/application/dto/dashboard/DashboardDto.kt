package com.taskmanager.application.dto.dashboard

import com.taskmanager.demo.model.enums.ProjectStatus
import java.math.BigDecimal

data class DashboardDto(
    // 1. Métricas de Projetos
    val totalProjects: Long,
    val projectsByStatus: Map<ProjectStatus, Long>,
    val projectsEndingSoon: List<ProjectSummaryDto>, // Projetos com prazo próximo (Ex: nos próximos 30 dias)

    // 2. Métricas de Tarefas
    val totalTasks: Long,
    val tasksOverdue: Long,
    val tasksInProgress: Long,
    val myPendingTasks: Long, // Tarefas atribuídas ao usuário logado

    // 3. Métricas Financeiras (Para ADMIN/GESTOR_OBRAS)
    val totalRevenue: BigDecimal?,
    val totalExpenses: BigDecimal?,
    val netBalance: BigDecimal?
)

/**
 * DTO auxiliar para resumir informações críticas de projetos no dashboard.
 */
data class ProjectSummaryDto(
    val id: String,
    val name: String,
    val status: ProjectStatus,
    val endDate: java.time.LocalDate?
)