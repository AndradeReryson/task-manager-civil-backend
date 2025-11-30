package com.taskmanager.presentation.controller

import com.taskmanager.application.dto.task.CreateTaskDto
import com.taskmanager.application.dto.task.TaskDto
import com.taskmanager.application.dto.task.UpdateTaskDto
import com.taskmanager.application.service.TaskService
import com.taskmanager.infrastructure.exception.CustomExceptions
import com.taskmanager.demo.security.CustomUserDetailsService
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
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService,
    private val customUserDetailsService: CustomUserDetailsService // Necessário para obter o Employee ID
) {

    /**
     * Helper para obter o Employee ID do usuário logado.
     */
    private fun getEmployeeIdFromPrincipal(principal: UserDetails): String {
        // Para simplificar, assumimos que o username do UserDetails é o mesmo do Employee (ou User ID)
        // Em um cenário real, precisaríamos buscar o Employee associado ao User.
        // Já que todo Employee é um User, usaremos o ID do User como Employee ID.
        val user = customUserDetailsService.loadUserByUsername(principal.username)
        return principal.username // Assumindo que o username é o ID do Employee temporariamente, ou buscaremos o Employee.
        // *** Nota de Correção: Em um sistema real, o UserDetails deve ter acesso ao ID do User (subject do JWT).
        // Usaremos o subject (ID) do JWT, que é o ID da nossa entidade User.
        // O Employee está ligado 1:1 ao User, então usaremos o ID do User como base.
    }
    
    // A implementação correta exigiria que o nosso UserDetails tivesse o ID do Employee,
    // mas, por enquanto, vamos passar o username como o valor a ser usado.
    private fun getUserIdFromPrincipal(principal: UserDetails): String {
        // Usamos o username do principal, que é o ID de login
        return principal.username 
    }

    /**
     * Endpoint: GET /api/tasks
     * Acesso: Todos que precisam ver as tarefas.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE', 'FUNCIONARIO')")
    fun findAll(
        @PageableDefault(size = 20, sort = ["dueDate", "priority"]) pageable: Pageable,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) assignedToId: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) priority: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false, defaultValue = "true") isActive: Boolean
    ): ResponseEntity<Page<TaskDto>> {
        val tasksPage = taskService.findAll(pageable, projectId, assignedToId, status, priority, search, isActive)
        return ResponseEntity.ok(tasksPage)
    }
    /**
     * Endpoint: GET /api/tasks/{id}
     * Acesso: Todos que precisam ver detalhes de uma tarefa.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE', 'FUNCIONARIO')")
    fun findById(@PathVariable id: String): ResponseEntity<TaskDto> {
        val task = taskService.findById(id)
        return ResponseEntity.ok(task)
    }
    
    /**
     * Endpoint: POST /api/tasks
     * Acesso: ADMIN, GESTOR_OBRAS, LIDER_EQUIPE, FUNCIONARIO (Todos podem reportar tarefas).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE', 'FUNCIONARIO')")
    fun create(
        @Valid @RequestBody dto: CreateTaskDto,
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<TaskDto> {
        // Passa o username (ex: "maria.santos") para o serviço.
        // O serviço usará este username para encontrar o Employee ID correspondente.
        val reporterUsername = principal.username 
        
        val newTask = taskService.create(dto, reporterUsername) // <-- Mudança: passa username
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask)
    }

    /**
     * Endpoint: PUT /api/tasks/{id}
     * Acesso: ADMIN, GESTOR_OBRAS, LIDER_EQUIPE (Pode editar), ou o ASSIGNED_TO (Pode atualizar status).
     * Nota: A lógica de autorização complexa deve ser tratada dentro do Service ou com @PostAuthorize mais detalhado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE')") // Simplificamos a regra no controller
    fun update(@PathVariable id: String, @Valid @RequestBody dto: UpdateTaskDto): ResponseEntity<TaskDto> {
        val updatedTask = taskService.update(id, dto)
        return ResponseEntity.ok(updatedTask)
    }

    /**
     * Endpoint: DELETE /api/tasks/{id}
     * Acesso: ADMIN, GESTOR_OBRAS, LIDER_EQUIPE (Soft Delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE')")
    fun delete(
        @PathVariable id: String, 
        @AuthenticationPrincipal principal: UserDetails 
    ): ResponseEntity<Void> {
        val deletedByUsername = getUserIdFromPrincipal(principal) // O ID do usuário logado
        taskService.softDelete(id, deletedByUsername)
        return ResponseEntity.noContent().build()
    }
}