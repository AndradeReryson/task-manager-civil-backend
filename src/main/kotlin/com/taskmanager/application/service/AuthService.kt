package com.taskmanager.application.service

import com.taskmanager.application.dto.auth.LoginRequestDto
import com.taskmanager.application.dto.auth.LoginResponseDto
import com.taskmanager.application.dto.auth.RefreshTokenDto
import com.taskmanager.application.dto.user.CreateUserDto
import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.UserRole
import com.taskmanager.demo.repository.UserRepository
import com.taskmanager.demo.security.JwtTokenProvider
import io.jsonwebtoken.Claims
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${app.jwt.access-expiration-ms}") 
    private val accessExpirationMs: Long // Injetando a duração para o Response DTO
) {

    /**
     * Realiza o processo de login: autentica o usuário e gera os tokens.
     */
    fun login(request: LoginRequestDto): LoginResponseDto {
        // 1. Autentica as credenciais
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        // 2. Obtém a entidade User (que implementa UserDetails)
        val user = authentication.principal as User
        
        // 3. Gera tokens
        val accessToken = jwtTokenProvider.generateAccessToken(authentication)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user)

        // 4. Retorna DTO de resposta, convertendo a expiração de MS para segundos
        val expiresInSeconds = TimeUnit.MILLISECONDS.toSeconds(accessExpirationMs)
        
        return LoginResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = expiresInSeconds
        )
    }
    
    /**
     * Renova o Access Token usando o Refresh Token.
     */
    fun refreshAccessToken(request: RefreshTokenDto): LoginResponseDto {
        val refreshToken = request.refreshToken
        
        // 1. Valida o Refresh Token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw IllegalArgumentException("Refresh Token inválido ou expirado.")
        }
        
        // 2. Obtém os Claims do Refresh Token
        val claims: Claims = jwtTokenProvider.getClaimsFromJWT(refreshToken)
        val userId = claims.subject
        
        // 3. Carrega o usuário para garantir que ele ainda existe e está ativo
        val user = userRepository.findById(userId)
            .orElseThrow { UsernameNotFoundException("Usuário do Refresh Token não encontrado.") }
        
        if (!user.isActive) {
            throw IllegalStateException("Usuário inativo.")
        }

        // 4. Gera novo Access Token
        // Nota: O Refresh Token não carrega claims de "roles" diretamente no nosso design.
        // Precisamos obtê-los do banco de dados (User entity) para o novo Access Token.
        val authenticationToken = UsernamePasswordAuthenticationToken(
            user, null, user.authorities
        )
        val newAccessToken = jwtTokenProvider.generateAccessToken(authenticationToken)
        
        // 5. Retorna a resposta, mantendo o Refresh Token antigo
        val expiresInSeconds = TimeUnit.MILLISECONDS.toSeconds(accessExpirationMs)
        
        return LoginResponseDto(
            accessToken = newAccessToken,
            refreshToken = refreshToken, // Mantém o mesmo refresh token
            expiresIn = expiresInSeconds
        )
    }

    /**
     * Cadastra o primeiro usuário com a role ADMIN (Usado no setup inicial).
     * Requer que não haja admins existentes para ser executado.
     */
    @Transactional
    fun registerInitialAdmin(request: CreateUserDto): User {
        // 1. Verifica se já existe um ADMIN
        if (userRepository.findAll().any { it.role == UserRole.ADMIN }) {
            throw IllegalStateException("Usuário ADMIN já cadastrado. Cadastro inicial bloqueado.")
        }
        
        // 2. Cria e salva o novo usuário ADMIN
        val adminUser = User(
            loginUsername = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            role = UserRole.ADMIN, // Garante que a role seja ADMIN
            fullName = request.fullName,
            email = request.email,
        )
        
        return userRepository.save(adminUser)
    }
}