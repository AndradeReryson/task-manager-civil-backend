package com.taskmanager.application.service

import com.taskmanager.application.dto.user.CreateUserDto
import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.UserRole
import com.taskmanager.demo.repository.UserRepository
import com.taskmanager.infrastructure.exception.ResourceConflictException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks lateinit var userService: UserService

    @Test
    fun `create deve lancar excecao quando username ja existe`() {
        // 1. ARRANGE
        val dto = CreateUserDto(
            username = "joao.duplicado",
            password = "123",
            fullName = "Joao",
            email = "j@j.com",
            role = UserRole.FUNCIONARIO
        )

        // Simula que o banco JÁ encontrou alguém com esse username
        val existingUser = User(loginUsername = "joao.duplicado", passwordHash = "...", role = UserRole.FUNCIONARIO, fullName = "X")
        `when`(userRepository.findByLoginUsername(dto.username)).thenReturn(existingUser)

        // 2. ACT & ASSERT
        // Esperamos que o serviço lance ResourceConflictException
        assertThrows(ResourceConflictException::class.java) {
            userService.create(dto)
        }
    }

    @Test
    fun `create deve salvar usuario quando username eh novo`() {
        // 1. ARRANGE
        val dto = CreateUserDto("novo.user", "123", "Novo", "n@n.com", UserRole.ADMIN)
        
        // Simula que não encontrou ninguém (null)
        `when`(userRepository.findByLoginUsername(dto.username)).thenReturn(null)
        `when`(passwordEncoder.encode(dto.password)).thenReturn("encoded_pass")
        
        // Simula o salvamento (retorna o objeto salvo com ID)
        `when`(userRepository.save(any(User::class.java))).thenAnswer { 
            val u = it.getArgument(0) as User
            u.copy(id = "generated-id") // Simula o ID gerado pelo banco
        }

        // 2. ACT
        val result = userService.create(dto)

        // 3. ASSERT
        kotlin.test.assertEquals("generated-id", result.id)
        kotlin.test.assertEquals("novo.user", result.username)
    }
}