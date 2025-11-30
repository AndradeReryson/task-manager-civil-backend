package com.taskmanager.presentation.controller

import com.taskmanager.application.dto.project.CreateProjectDto
import com.taskmanager.application.dto.project.ProjectDto
import com.taskmanager.application.dto.project.UpdateProjectDto
import com.taskmanager.application.service.ProjectService
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
@RequestMapping("/api/projects")
class ProjectController(
    private val projectService: ProjectService
) {

    /**
     * Endpoint: GET /api/projects
     * Acesso: Todos que precisam ver a lista de projetos ativos.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE', 'FUNCIONARIO')")
    fun findAll(
        @PageableDefault(size = 20, sort = ["name"]) pageable: Pageable,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false, defaultValue = "true") isActive: Boolean
    ): ResponseEntity<Page<ProjectDto>> {
        val projectsPage = projectService.findAll(pageable, status, search, isActive)
        return ResponseEntity.ok(projectsPage)
    }

    /**
     * Endpoint: GET /api/projects/{id}
     * Acesso: Todos que precisam ver os detalhes de um projeto específico.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE', 'FUNCIONARIO')")
    fun findById(@PathVariable id: String): ResponseEntity<ProjectDto> {
        val project = projectService.findById(id)
        return ResponseEntity.ok(project)
    }
    
    /**
     * Endpoint: POST /api/projects
     * Acesso: ADMIN, GESTOR_OBRAS
     * Cria um novo projeto.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')")
    fun create(@Valid @RequestBody dto: CreateProjectDto): ResponseEntity<ProjectDto> {
        val newProject = projectService.create(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(newProject)
    }

    /**
     * Endpoint: PUT /api/projects/{id}
     * Acesso: ADMIN, GESTOR_OBRAS
     * Atualiza um projeto.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')")
    fun update(@PathVariable id: String, @Valid @RequestBody dto: UpdateProjectDto): ResponseEntity<ProjectDto> {
        val updatedProject = projectService.update(id, dto)
        return ResponseEntity.ok(updatedProject)
    }

    /**
     * Endpoint: DELETE /api/projects/{id}
     * Acesso: ADMIN, GESTOR_OBRAS (Apenas para soft delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')")
    fun delete(
        @PathVariable id: String, 
        @AuthenticationPrincipal principal: UserDetails 
    ): ResponseEntity<Void> {
        projectService.softDelete(id, principal.username)
        return ResponseEntity.noContent().build()
    }
}