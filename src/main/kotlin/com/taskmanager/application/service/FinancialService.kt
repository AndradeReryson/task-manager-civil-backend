package com.taskmanager.application.service

import com.taskmanager.application.dto.financial.CreateFinancialDto
import com.taskmanager.application.dto.financial.FinancialDto
import com.taskmanager.application.dto.financial.UpdateFinancialDto
import com.taskmanager.demo.model.Financial
import com.taskmanager.demo.model.Employee
import com.taskmanager.demo.model.Project
import com.taskmanager.demo.repository.EmployeeRepository
import com.taskmanager.demo.repository.FinancialRepository
import com.taskmanager.demo.repository.ProjectRepository
import com.taskmanager.infrastructure.exception.CustomExceptions
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

import com.taskmanager.infrastructure.filter.SearchCriteria // Import
import org.springframework.data.jpa.domain.Specification // Import

@Service
class FinancialService(
    private val financialRepository: FinancialRepository,
    private val projectRepository: ProjectRepository,
    private val employeeRepository: EmployeeRepository
) {

    /**
     * Converte a Entidade Financial para o DTO de resposta.
     */
    private fun Financial.toDto(): FinancialDto {
      val financialId = this.id ?: throw IllegalStateException("Entidade Financial deve ter um ID para ser convertido para DTO.")

      return FinancialDto(
          id = financialId,
          description = this.description,
          type = this.type,
          category = this.category,
          amount = this.amount,
          transactionDate = this.transactionDate,
          projectId = this.project?.id,
          employeeId = this.employee?.id,
          receiptUrl = this.receiptUrl,
          isActive = this.isActive,
          createdAt = this.createdAt
      )
    }

    // --- Helpers de Busca ---

    private fun findProjectById(id: String): Project {
        return projectRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Projeto", "id", id) }
    }
    
    private fun findEmployeeById(id: String): Employee {
        return employeeRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Colaborador", "id", id) }
    }

    /**
     * Cria um novo registro Financial.
     */
    @Transactional
    fun create(dto: CreateFinancialDto): FinancialDto {
        // 1. Validações de referências externas (se fornecidas)
        val project = dto.projectId?.let { findProjectById(it) }
        val employee = dto.employeeId?.let { findEmployeeById(it) }

        // 2. Cria e salva o registro financeiro
        val newFinancial = Financial(
            description = dto.description,
            type = dto.type,
            category = dto.category,
            amount = dto.amount,
            transactionDate = dto.transactionDate,
            project = project,
            employee = employee,
            receiptUrl = dto.receiptUrl
        )
        val savedFinancial = financialRepository.save(newFinancial)

        return savedFinancial.toDto()
    }

    /**
     * Busca um registro Financial pelo ID.
     */
    fun findById(id: String): FinancialDto {
        val financial = financialRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Registro Financeiro", "id", id) }
        return financial.toDto()
    }

    /**
     * Atualiza um registro Financial existente.
     */
    @Transactional
    fun update(id: String, dto: UpdateFinancialDto): FinancialDto {
        val financial = financialRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Registro Financeiro", "id", id) }
        
        // 1. Atualiza campos simples
        dto.description?.let { financial.description = it }
        dto.type?.let { financial.type = it }
        dto.category?.let { financial.category = it }
        dto.amount?.let { financial.amount = it }
        dto.transactionDate?.let { financial.transactionDate = it }
        dto.receiptUrl?.let { financial.receiptUrl = it }
        dto.isActive?.let { financial.isActive = it }

        // 2. Atualiza referências (com validação)
        if (dto.projectId != null) {
            financial.project = findProjectById(dto.projectId)
        } else if (dto.projectId == null) {
            financial.project = null // Permite desvincular
        }

        if (dto.employeeId != null) {
            financial.employee = findEmployeeById(dto.employeeId)
        } else if (dto.employeeId == null) {
            financial.employee = null // Permite desvincular
        }
        
        val updatedFinancial = financialRepository.save(financial)
        return updatedFinancial.toDto()
    }

    /**
     * Lista registros financeiros com paginação e filtros dinâmicos.
     */
    fun findAll(
        pageable: Pageable,
        type: String? = null,
        category: String? = null,
        projectId: String? = null,
        employeeId: String? = null,
        description: String? = null,
        isActive: Boolean = true
    ): Page<FinancialDto> {
        val criteriaList = mutableListOf<SearchCriteria>()

        type?.let { criteriaList.add(SearchCriteria("type", ":", it)) }
        category?.let { criteriaList.add(SearchCriteria("category", ":", it)) }
        projectId?.let { criteriaList.add(SearchCriteria("project.id", ":", it)) }
        employeeId?.let { criteriaList.add(SearchCriteria("employee.id", ":", it)) }
        description?.let { criteriaList.add(SearchCriteria("description", "like", it)) }
        
        criteriaList.add(SearchCriteria("isActive", ":", isActive))

        val spec = FinancialSpecification(criteriaList)
        return financialRepository.findAll(spec, pageable).map { it.toDto() }
    }
    
    /**
     * Implementa o Soft Delete.
     */
    @Transactional
    fun softDelete(id: String, deletedByUsername: String) {
        val financial = financialRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Registro Financeiro", "id", id) }

        financial.isActive = false
        financial.deletedAt = LocalDateTime.now()
        financial.deletedBy = deletedByUsername
        financialRepository.save(financial)
    }
}