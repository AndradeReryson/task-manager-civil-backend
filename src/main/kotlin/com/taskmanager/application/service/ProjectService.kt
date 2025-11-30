package com.taskmanager.application.service

import com.taskmanager.application.dto.project.CreateProjectDto
import com.taskmanager.application.dto.project.ProjectDto
import com.taskmanager.application.dto.project.UpdateProjectDto
import com.taskmanager.demo.model.Project
import com.taskmanager.demo.model.enums.ProjectStatus
import com.taskmanager.demo.repository.EmployeeRepository
import com.taskmanager.demo.repository.ProjectRepository
import com.taskmanager.infrastructure.exception.CustomExceptions
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

import com.taskmanager.infrastructure.filter.SearchCriteria 
import org.springframework.data.jpa.domain.Specification

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val employeeRepository: EmployeeRepository
) {

    /**
     * Converte a Entidade Project para o DTO de resposta.
     */
    private fun Project.toDto(): ProjectDto {
      val projectId = this.id ?: throw IllegalStateException("Projeto deve ter um ID para ser convertido para DTO.")
      
      return ProjectDto(
          id = projectId,
          name = this.name,
          description = this.description,
          status = this.status,
          startDate = this.startDate,
          endDate = this.endDate,
          actualEndDate = this.actualEndDate,
          budget = this.budget,
          managerId = this.manager?.id, // Obtém apenas o ID do gestor
          isActive = this.isActive,
          createdAt = this.createdAt
      )
    }

    /**
     * Cria um novo Project.
     */
    @Transactional
    fun create(dto: CreateProjectDto): ProjectDto {
        // 1. Valida unicidade do nome
        projectRepository.findByName(dto.name)?.let {
            throw CustomExceptions.ResourceConflictException("Projeto com nome '${dto.name}' já existe.")
        }
        
        // 2. Busca e valida o gestor (se fornecido)
        val manager = dto.managerId?.let { managerId ->
            employeeRepository.findById(managerId)
                .orElseThrow { CustomExceptions.ResourceNotFoundException("Colaborador (Gerente)", "id", managerId) }
        }

        // 3. Cria e salva o projeto
        val newProject = Project(
            name = dto.name,
            description = dto.description,
            status = dto.status, // Geralmente será ProjectStatus.PLANEJAMENTO
            startDate = dto.startDate,
            endDate = dto.endDate,
            budget = dto.budget,
            manager = manager
        )
        val savedProject = projectRepository.save(newProject)

        return savedProject.toDto()
    }

    /**
     * Busca um Project pelo ID.
     */
    fun findById(id: String): ProjectDto {
        val project = projectRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Projeto", "id", id) }
        return project.toDto()
    }

    /**
     * Atualiza um Project existente.
     */
    @Transactional
    fun update(id: String, dto: UpdateProjectDto): ProjectDto {
        val project = projectRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Projeto", "id", id) }
        
        // 1. Atualiza campos simples
        dto.name?.let { project.name = it }
        dto.description?.let { project.description = it }
        dto.status?.let { project.status = it }
        dto.startDate?.let { project.startDate = it }
        dto.endDate?.let { project.endDate = it }
        dto.actualEndDate?.let { project.actualEndDate = it }
        dto.budget?.let { project.budget = it }
        dto.isActive?.let { project.isActive = it }

        // 2. Atualiza o gestor (manager)
        if (dto.managerId != null) {
            val newManager = employeeRepository.findById(dto.managerId)
                .orElseThrow { CustomExceptions.ResourceNotFoundException("Colaborador (Gerente)", "id", dto.managerId) }
            project.manager = newManager
        }
        
        val updatedProject = projectRepository.save(project)
        return updatedProject.toDto()
    }

    /**
     * Lista todos os Projects com paginação e ordenação.
     * Deve ser filtrável por status, nome, etc. (futuro).
     */
    fun findAll(
        pageable: Pageable, 
        status: String? = null, 
        search: String? = null,
        isActive: Boolean = true // <--- NOVO
    ): Page<ProjectDto> {
        val criteriaList = mutableListOf<SearchCriteria>()

        status?.let { criteriaList.add(SearchCriteria("status", ":", it)) }
        search?.let { criteriaList.add(SearchCriteria("name", "like", it)) }
        
        // 🚨 ALTERAÇÃO: Filtro dinâmico de Lixeira/Ativos
        criteriaList.add(SearchCriteria("isActive", ":", isActive))

        val spec = ProjectSpecification(criteriaList)
        // Nota: Removemos o .and(ProjectSpecification.activeProjects()) pois o critério acima já resolve.

        return projectRepository.findAll(spec, pageable).map { it.toDto() }
    }
    
    /**
     * Implementa o Soft Delete.
     */
    @Transactional
    fun softDelete(id: String, deletedByUsername: String) {
        val project = projectRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Projeto", "id", id) }

        project.isActive = false
        project.deletedAt = LocalDateTime.now()
        project.deletedBy = deletedByUsername
        projectRepository.save(project)
    }
}