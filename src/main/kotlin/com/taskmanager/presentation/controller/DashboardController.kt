package com.taskmanager.presentation.controller

import com.taskmanager.application.dto.dashboard.DashboardDto
import com.taskmanager.application.service.DashboardService
import com.taskmanager.demo.model.enums.UserRole
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {

    /**
     * Endpoint: GET /api/dashboard
     * Acesso: Todos os usuários autenticados.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()") 
    fun getDashboardMetrics(@AuthenticationPrincipal principal: UserDetails): ResponseEntity<DashboardDto> {
        
        // 1. Determina se o usuário tem permissão para dados financeiros
        val isAdminOrGestor = principal.authorities.any { 
            it.authority == UserRole.ADMIN.withPrefix() || it.authority == UserRole.GESTOR_OBRAS.withPrefix() 
        }

        // 2. O username do principal é o ID do nosso User.
        val currentUserId = principal.username
        
        // 3. Gera o dashboard
        val dashboardDto = dashboardService.getGlobalDashboard(currentUserId, isAdminOrGestor)
        
        return ResponseEntity.ok(dashboardDto)
    }
}