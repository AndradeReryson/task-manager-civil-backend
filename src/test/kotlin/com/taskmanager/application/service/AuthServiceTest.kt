package com.taskmanager.application.service

import com.taskmanager.application.dto.auth.LoginRequestDto
import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.UserRole
import com.taskmanager.demo.repository.UserRepository
import com.taskmanager.demo.security.JwtTokenProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var passwordEncoder: PasswordEncoder
    @Mock lateinit var authenticationManager: AuthenticationManager
    @Mock lateinit var jwtTokenProvider: JwtTokenProvider

    // A classe que estamos testando
    lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        // Instanciação manual para injetar o valor de @Value (accessExpirationMs)
        // Passamos 3600000L (1 hora) como tempo de expiração fictício
        authService = AuthService(
            userRepository,
            passwordEncoder,
            authenticationManager,
            jwtTokenProvider,
            3600000L
        )
    }

    @Test
    fun `login deve retornar tokens quando credenciais sao validas`() {
        // 1. ARRANGE (Preparação)
        val loginRequest = LoginRequestDto("admin", "password")
        
        // Mocks para simular o sucesso da autenticação
        val authMock = mock(Authentication::class.java)
        val userMock = User(
            id = "1",
            loginUsername = "admin", // Nome correto da propriedade
            passwordHash = "hash_secreto",
            role = UserRole.ADMIN,
            fullName = "Administrador",
            email = "admin@teste.com"
        )

        // Configura o comportamento dos Mocks
        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenReturn(authMock)
        
        `when`(authMock.principal).thenReturn(userMock)
        `when`(jwtTokenProvider.generateAccessToken(authMock)).thenReturn("access-token-valido")
        `when`(jwtTokenProvider.generateRefreshToken(userMock)).thenReturn("refresh-token-valido")

        // 2. ACT (Execução)
        val response = authService.login(loginRequest)

        // 3. ASSERT (Verificação)
        assertNotNull(response)
        assertEquals("access-token-valido", response.accessToken)
        assertEquals("refresh-token-valido", response.refreshToken)
        
        // Verifica se o authenticationManager foi chamado exatamente uma vez
        verify(authenticationManager, times(1)).authenticate(any())
    }
}