package com.taskmanager.presentation.controller

import com.taskmanager.application.dto.financial.CreateFinancialDto
import com.taskmanager.application.dto.financial.FinancialDto
import com.taskmanager.application.dto.financial.UpdateFinancialDto
import com.taskmanager.application.service.FinancialService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/financial")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')") // Acesso restrito
class FinancialController(
    private val financialService: FinancialService
) {
    /**
     * Endpoint: GET /api/financial
     * Acesso: ADMIN, GESTOR_OBRAS
     * Suporta filtros: type, category, projectId, employeeId, description
     */
    @GetMapping
    fun findAll(
        @PageableDefault(size = 20, sort = ["transactionDate", "type"]) pageable: Pageable,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) employeeId: String?,
        @RequestParam(required = false) description: String?, // Busca por texto
        @RequestParam(required = false, defaultValue = "true") isActive: Boolean
    ): ResponseEntity<Page<FinancialDto>> {
        
        val financialPage = financialService.findAll(
            pageable, type, category, projectId, employeeId, description, isActive
        )
        return ResponseEntity.ok(financialPage)
    }

    /**
     * Endpoint: GET /api/financial/{id}
     * Acesso: ADMIN, GESTOR_OBRAS
     */
    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): ResponseEntity<FinancialDto> {
        val financial = financialService.findById(id)
        return ResponseEntity.ok(financial)
    }
    
    /**
     * Endpoint: POST /api/financial
     * Acesso: ADMIN, GESTOR_OBRAS
     * Cria um novo registro.
     */
    @PostMapping
    fun create(@Valid @RequestBody dto: CreateFinancialDto): ResponseEntity<FinancialDto> {
        val newFinancial = financialService.create(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(newFinancial)
    }

    /**
     * Endpoint: PUT /api/financial/{id}
     * Acesso: ADMIN, GESTOR_OBRAS
     * Atualiza um registro.
     */
    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @Valid @RequestBody dto: UpdateFinancialDto): ResponseEntity<FinancialDto> {
        val updatedFinancial = financialService.update(id, dto)
        return ResponseEntity.ok(updatedFinancial)
    }

    /**
     * Endpoint: DELETE /api/financial/{id}
     * Acesso: ADMIN, GESTOR_OBRAS (Soft Delete).
     */
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: String, 
        @AuthenticationPrincipal principal: UserDetails 
    ): ResponseEntity<Void> {
        // Helper para obter o username do usuário logado
        val deletedByUsername = principal.username
        financialService.softDelete(id, deletedByUsername)
        return ResponseEntity.noContent().build()
    }
}