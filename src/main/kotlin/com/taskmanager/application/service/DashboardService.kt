package com.taskmanager.application.service

import com.taskmanager.application.dto.dashboard.DashboardDto
import com.taskmanager.application.dto.dashboard.ProjectSummaryDto
import com.taskmanager.demo.model.enums.ProjectStatus
import com.taskmanager.demo.repository.FinancialRepository
import com.taskmanager.demo.repository.ProjectRepository
import com.taskmanager.demo.repository.TaskRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class DashboardService(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val financialRepository: FinancialRepository
) {

    /**
     * Gera o DashboardDto com métricas agregadas.
     * @param currentUserId O ID do usuário logado (usado para métricas pessoais como myPendingTasks).
     * @param isAdminOrGestor Indica se o usuário tem permissão para ver dados financeiros.
     */
    fun getGlobalDashboard(currentUserId: String, isAdminOrGestor: Boolean): DashboardDto {
        // --- 1. Métricas de Projetos ---
        val allProjects = projectRepository.findAll()
        val totalProjects = allProjects.size.toLong()
        
        // Agrupamento por Status
        val projectsByStatus = allProjects
            .groupingBy { it.status }
            .eachCount()
            .mapValues { it.value.toLong() }
        
        // Projetos com prazo próximo (Ex: nos próximos 30 dias)
        val today = LocalDate.now()
        val deadlineLimit = today.plusDays(30)

        val projectsEndingSoon = allProjects
            .filter { it.endDate != null && it.endDate!!.isBefore(deadlineLimit) && it.status == ProjectStatus.EM_ANDAMENTO }
            .map { 
                ProjectSummaryDto(
                    id = it.id!!, 
                    name = it.name, 
                    status = it.status, 
                    endDate = it.endDate 
                ) 
            }
            .sortedBy { it.endDate }


        // --- 2. Métricas de Tarefas ---
        val allTasks = taskRepository.findAll()
        val totalTasks = allTasks.size.toLong()

        val tasksOverdue = allTasks.count { 
            it.dueDate != null && it.dueDate!!.isBefore(LocalDateTime.now()) && it.status != com.taskmanager.demo.model.enums.TaskStatus.CONCLUIDA 
        }.toLong()

        val tasksInProgress = allTasks.count { 
            it.status == com.taskmanager.demo.model.enums.TaskStatus.EM_PROGRESSO 
        }.toLong()

        // Tarefas atribuídas ao usuário logado (usando o ID do User como Employee ID)
        val myPendingTasks = allTasks.count { 
            it.assignedTo?.user?.id == currentUserId && it.status != com.taskmanager.demo.model.enums.TaskStatus.CONCLUIDA 
        }.toLong()

        // --- 3. Métricas Financeiras ---
        var totalRevenue: BigDecimal? = null
        var totalExpenses: BigDecimal? = null
        var netBalance: BigDecimal? = null

        if (isAdminOrGestor) {
            val allFinancial = financialRepository.findAll()
            
            totalRevenue = allFinancial
                .filter { it.type.name == "RECEITA" }
                .sumOf { it.amount }
            
            // Assume que DESPESA e CUSTO são saídas
            val outgoing = allFinancial
                .filter { it.type.name == "DESPESA" || it.type.name == "CUSTO" }
                .sumOf { it.amount }
            
            totalExpenses = outgoing
            netBalance = totalRevenue.minus(outgoing)
        }

        // --- 4. Construção do DTO ---
        return DashboardDto(
            totalProjects = totalProjects,
            projectsByStatus = projectsByStatus,
            projectsEndingSoon = projectsEndingSoon,
            totalTasks = totalTasks,
            tasksOverdue = tasksOverdue,
            tasksInProgress = tasksInProgress,
            myPendingTasks = myPendingTasks,
            totalRevenue = totalRevenue,
            totalExpenses = totalExpenses,
            netBalance = netBalance
        )
    }
}