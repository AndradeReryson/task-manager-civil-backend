package com.taskmanager.presentation.controller

import com.taskmanager.application.dto.team.CreateTeamDto
import com.taskmanager.application.dto.team.TeamDto
import com.taskmanager.application.dto.team.UpdateTeamDto
import com.taskmanager.application.service.TeamService
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
@RequestMapping("/api/teams")
class TeamController(
    private val teamService: TeamService
) {

    /**
     * Endpoint: GET /api/teams
     * Acesso: ADMIN, GESTOR_OBRAS, LIDER_EQUIPE
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE')")
    fun findAll(
        @PageableDefault(size = 20, sort = ["name"]) pageable: Pageable,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) leaderId: String?,
        @RequestParam(required = false, defaultValue = "true") isActive: Boolean // <--- NOVO
    ): ResponseEntity<Page<TeamDto>> {
        
        val teamsPage = teamService.findAll(pageable, name, leaderId, isActive)
        return ResponseEntity.ok(teamsPage)
    }

    
    /**
     * Endpoint: GET /api/teams/{id}
     * Acesso: ADMIN, GESTOR_OBRAS, LIDER_EQUIPE
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE')")
    fun findById(@PathVariable id: String): ResponseEntity<TeamDto> {
        val team = teamService.findById(id)
        return ResponseEntity.ok(team)
    }
    
    /**
     * Endpoint: POST /api/teams
     * Acesso: ADMIN, GESTOR_OBRAS
     * Cria uma nova equipe.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')")
    fun create(@Valid @RequestBody dto: CreateTeamDto): ResponseEntity<TeamDto> {
        val newTeam = teamService.create(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(newTeam)
    }

    /**
     * Endpoint: PUT /api/teams/{id}
     * Acesso: ADMIN, GESTOR_OBRAS
     * Atualiza uma equipe.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')")
    fun update(@PathVariable id: String, @Valid @RequestBody dto: UpdateTeamDto): ResponseEntity<TeamDto> {
        val updatedTeam = teamService.update(id, dto)
        return ResponseEntity.ok(updatedTeam)
    }

    /**
     * Endpoint: DELETE /api/teams/{id}
     * Acesso: ADMIN, GESTOR_OBRAS (Soft Delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')")
    fun delete(
        @PathVariable id: String, 
        @AuthenticationPrincipal principal: UserDetails 
    ): ResponseEntity<Void> {
        teamService.softDelete(id, principal.username)
        return ResponseEntity.noContent().build()
    }
}