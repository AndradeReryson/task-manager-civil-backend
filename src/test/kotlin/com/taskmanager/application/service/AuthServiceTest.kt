package com.taskmanager.application.service

import com.taskmanager.application.dto.auth.LoginRequestDto
import com.taskmanager.demo.config.JwtProperties
import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.UserRole
import com.taskmanager.demo.repository.UserRepository
import com.taskmanager.demo.security.JwtTokenProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class) // Habilita o Mockito
class AuthServiceTest {

    // Mocks: Dependências simuladas
    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var passwordEncoder: PasswordEncoder
    @Mock lateinit var authenticationManager: AuthenticationManager
    @Mock lateinit var jwtTokenProvider: JwtTokenProvider
    
    // InjectMocks: A classe real que estamos testando (recebe os mocks)
    @InjectMocks lateinit var authService: AuthService

    @org.junit.jupiter.api.BeforeEach
    fun setup() {
        // Instancia manualmente passando um valor fixo para a expiração (ex: 3600000L)
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
        // 1. PREPARAÇÃO (Arrange)
        val request = LoginRequestDto("admin", "password")
        val authMock = mock(Authentication::class.java)
        val userMock = User(
            id = "1", 
            loginUsername = "admin", 
            passwordHash = "hash", 
            role = UserRole.ADMIN, 
            fullName = "Admin", 
            email = "admin@test.com"
        )

        // Quando o authenticationManager for chamado, retorne sucesso
        `when`(authenticationManager.authenticate(any())).thenReturn(authMock)
        `when`(authMock.principal).thenReturn(userMock)
        
        // Quando pedir tokens, retorne strings fictícias
        `when`(jwtTokenProvider.generateAccessToken(authMock)).thenReturn("access-token-fake")
        `when`(jwtTokenProvider.generateRefreshToken(userMock)).thenReturn("refresh-token-fake")

        // 2. EXECUÇÃO (Act)
        // Nota: Se o AuthService falhar por causa do @Value não injetado, 
        // precisaremos ajustar a criação da classe. Assumindo construtor padrão por enquanto.
        val response = authService.login(request)

        // 3. VERIFICAÇÃO (Assert)
        assertNotNull(response)
        assertEquals("access-token-fake", response.accessToken)
        assertEquals("refresh-token-fake", response.refreshToken)
        
        // Verifica se o authenticationManager foi chamado exatamente uma vez
        verify(authenticationManager, times(1)).authenticate(any())
    }
}