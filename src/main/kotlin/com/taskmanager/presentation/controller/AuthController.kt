package com.taskmanager.presentation.controller

import com.taskmanager.application.dto.auth.LoginRequestDto
import com.taskmanager.application.dto.auth.LoginResponseDto
import com.taskmanager.application.dto.auth.RefreshTokenDto
import com.taskmanager.application.dto.user.CreateUserDto
import com.taskmanager.application.dto.user.UserDto
import com.taskmanager.application.service.AuthService
import com.taskmanager.demo.model.enums.UserRole
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    /**
     * Endpoint: POST /api/auth/login
     * Função: Autentica o usuário e retorna o Access Token e Refresh Token.
     */
    @PostMapping("/login")
    fun authenticateUser(@Valid @RequestBody loginRequest: LoginRequestDto): ResponseEntity<LoginResponseDto> {
        try {
            val responseDto = authService.login(loginRequest)
            return ResponseEntity.ok(responseDto)
        } catch (e: Exception) {
            // Captura falhas de autenticação (UsernameNotFoundException ou BadCredentialsException)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas ou usuário inativo.")
        }
    }

    /**
     * Endpoint: POST /api/auth/refresh
     * Função: Renova o Access Token usando o Refresh Token.
     */
    @PostMapping("/refresh")
    fun refreshAccessToken(@Valid @RequestBody refreshTokenDto: RefreshTokenDto): ResponseEntity<LoginResponseDto> {
        try {
            val responseDto = authService.refreshAccessToken(refreshTokenDto)
            return ResponseEntity.ok(responseDto)
        } catch (e: Exception) {
            // Captura token inválido ou expirado
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido, expirado ou usuário inativo.")
        }
    }
    
    /**
     * Endpoint: POST /api/auth/register-admin
     * Função: Cadastra o primeiro usuário ADMINISTRADOR. Protegido pela regra de 'somente se não houver ADMIN'.
     */
    @PostMapping("/register-admin")
    fun registerAdmin(@Valid @RequestBody createAdminDto: CreateUserDto): ResponseEntity<UserDto> {
        if (createAdminDto.role != UserRole.ADMIN) {
             throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Este endpoint só pode registrar a role ADMIN.")
        }
        
        try {
            val adminUser = authService.registerInitialAdmin(createAdminDto)
            
            // Mapeia para UserDto para evitar expor a senha no retorno
            val responseDto = UserDto(
                id = adminUser.id!!, 
                username = adminUser.username, 
                fullName = adminUser.fullName,
                email = adminUser.email,
                role = adminUser.role,
                isActive = adminUser.isActive,
                createdAt = adminUser.createdAt
            )
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto)
        } catch (e: IllegalStateException) {
            // Captura erro de ADMIN já existente
            throw ResponseStatusException(HttpStatus.CONFLICT, e.message)
        }
    }
}