package com.taskmanager.application.service

import com.taskmanager.application.dto.task.CreateTaskDto
import com.taskmanager.application.dto.task.TaskDto
import com.taskmanager.application.dto.task.UpdateTaskDto
import com.taskmanager.demo.model.Employee
import com.taskmanager.demo.model.Project
import com.taskmanager.demo.model.Task
import com.taskmanager.demo.repository.EmployeeRepository
import com.taskmanager.demo.repository.ProjectRepository
import com.taskmanager.demo.repository.TaskRepository
import com.taskmanager.infrastructure.exception.CustomExceptions
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

import com.taskmanager.infrastructure.filter.SearchCriteria 
import org.springframework.data.jpa.domain.Specification

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val employeeRepository: EmployeeRepository,
) {

    /**
     * Converte a Entidade Task para o DTO de resposta.
     */
    private fun Task.toDto(): TaskDto {
      val taskId = this.id ?: throw IllegalStateException("Tarefa deve ter um ID para ser convertido para DTO.")
      val projectId = this.project.id ?: throw IllegalStateException("Tarefa deve ter um ID de projeto para ser convertido para DTO.")

      return TaskDto(
          id = taskId,
          title = this.title,
          description = this.description,
          projectId = projectId,
          assignedToId = this.assignedTo?.id,
          reporterId = this.reporter?.id,
          status = this.status,
          priority = this.priority,
          dueDate = this.dueDate,
          completionDate = this.completionDate,
          isActive = this.isActive,
          createdAt = this.createdAt
      )
  }

    /**
     * Valida e busca a entidade Project ou Employee pelo ID.
     */
    private fun findProjectById(id: String): Project {
        return projectRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Projeto", "id", id) }
    }
    
    private fun findEmployeeById(id: String): Employee {
        return employeeRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Colaborador", "id", id) }
    }

    private fun findEmployeeByUsername(username: String): Employee {
        return employeeRepository.findByUser_LoginUsername(username)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Colaborador (Repórter)", "username", username) }
    }

    /**
     * Cria uma nova Task.
     */
    @Transactional
    fun create(dto: CreateTaskDto, reporterUsername: String): TaskDto {
        // 1. Validações de referências externas
        val project = findProjectById(dto.projectId)
        
        val assignedTo = dto.assignedToId?.let { findEmployeeById(it) }
        
        // O Reporter é o usuário logado que criou a tarefa
        val reporter = findEmployeeByUsername(reporterUsername)

        // 2. Cria e salva a tarefa
        val newTask = Task(
            title = dto.title,
            description = dto.description,
            project = project,
            assignedTo = assignedTo,
            reporter = reporter,
            status = dto.status,
            priority = dto.priority,
            dueDate = dto.dueDate
        )
        val savedTask = taskRepository.save(newTask)

        return savedTask.toDto()
    }

    /**
     * Busca uma Task pelo ID.
     */
    fun findById(id: String): TaskDto {
        val task = taskRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Tarefa", "id", id) }
        return task.toDto()
    }

    /**
     * Atualiza uma Task existente.
     */
    @Transactional
    fun update(id: String, dto: UpdateTaskDto): TaskDto {
        val task = taskRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Tarefa", "id", id) }
        
        // 1. Atualiza campos de referências (com validação)
        dto.projectId?.let { task.project = findProjectById(it) }
        dto.assignedToId?.let { task.assignedTo = findEmployeeById(it) } ?: run { task.assignedTo = null } // Permite desatribuir
        
        // 2. Atualiza campos simples
        dto.title?.let { task.title = it }
        dto.description?.let { task.description = it }
        dto.status?.let { task.status = it }
        dto.priority?.let { task.priority = it }
        dto.dueDate?.let { task.dueDate = it }
        dto.completionDate?.let { task.completionDate = it }
        dto.isActive?.let { task.isActive = it }

        // 3. Lógica de conclusão automática: Se o status for CONCLUIDA e completionDate for nulo, defina agora.
        if (task.status.name == "CONCLUIDA" && task.completionDate == null) {
            task.completionDate = LocalDateTime.now()
        }
        
        val updatedTask = taskRepository.save(task)
        return updatedTask.toDto()
    }

    /**
     * Lista todas as Tasks com paginação, filtragem.
     */
    fun findAll(
        pageable: Pageable, 
        projectId: String? = null, 
        assignedToId: String? = null,
        status: String? = null,
        priority: String? = null,
        search: String? = null,
        isActive: Boolean = true
    ): Page<TaskDto> {
        val criteriaList = mutableListOf<SearchCriteria>()

        projectId?.let { criteriaList.add(SearchCriteria("project.id", ":", it)) }
        assignedToId?.let { criteriaList.add(SearchCriteria("assignedTo.id", ":", it)) }
        status?.let { criteriaList.add(SearchCriteria("status", ":", it)) }
        priority?.let { criteriaList.add(SearchCriteria("priority", ":", it)) }
        search?.let { criteriaList.add(SearchCriteria("title", "like", it)) }
      
        criteriaList.add(SearchCriteria("isActive", ":", isActive))

        val spec = TaskSpecification(criteriaList)
        return taskRepository.findAll(spec, pageable).map { it.toDto() }
    }
    
    /**
     * Implementa o Soft Delete.
     */
    @Transactional
    fun softDelete(id: String, deletedByUsername: String) {
        val task = taskRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Tarefa", "id", id) }

        task.isActive = false
        task.deletedAt = LocalDateTime.now()
        task.deletedBy = deletedByUsername
        taskRepository.save(task)
    }
}