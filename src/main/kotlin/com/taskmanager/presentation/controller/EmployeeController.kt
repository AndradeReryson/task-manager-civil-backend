package com.taskmanager.presentation.controller

import com.taskmanager.application.dto.employee.CreateEmployeeDto
import com.taskmanager.application.dto.employee.EmployeeDto
import com.taskmanager.application.dto.employee.UpdateEmployeeDto
import com.taskmanager.application.service.EmployeeService
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
@RequestMapping("/api/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {

    /**
     * Endpoint: GET /api/employees
     * Função: Lista colaboradores com suporte a paginação e FILTROS AVANÇADOS.
     * * Exemplo de uso: 
     * GET /api/employees?department=ENGENHARIA&status=ACTIVE&search=João&page=0&size=10
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE')")
    fun findAll(
        @PageableDefault(size = 20, sort = ["user.fullName"]) pageable: Pageable,
        @RequestParam(required = false) department: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false, defaultValue = "true") isActive: Boolean
    ): ResponseEntity<Page<EmployeeDto>> {
        
        // Passa os parâmetros opcionais para o serviço que usa Specifications
        val employeesPage = employeeService.findAll(pageable, department, status, search, isActive)
        
        return ResponseEntity.ok(employeesPage)
    }

    // --- Outros métodos (findById, create, update, delete) permanecem inalterados ---

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE') or principal.username == @employeeService.findById(#id).email") 
    // Nota: A expressão de segurança acima é complexa; simplifique para 'permitAll' ou lógica no service se der erro.
    // Para simplificar aqui, mantemos a role check básica:
    // @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE')")
    fun findById(@PathVariable id: String): ResponseEntity<EmployeeDto> {
        val employee = employeeService.findById(id)
        return ResponseEntity.ok(employee)
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')")
    fun create(@Valid @RequestBody dto: CreateEmployeeDto): ResponseEntity<EmployeeDto> {
        val newEmployee = employeeService.create(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(newEmployee)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')")
    fun update(@PathVariable id: String, @Valid @RequestBody dto: UpdateEmployeeDto): ResponseEntity<EmployeeDto> {
        val updatedEmployee = employeeService.update(id, dto)
        return ResponseEntity.ok(updatedEmployee)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(
        @PathVariable id: String, 
        @AuthenticationPrincipal principal: UserDetails 
    ): ResponseEntity<Void> {
        employeeService.softDelete(id, principal.username)
        return ResponseEntity.noContent().build()
    }
}