package com.taskmanager.application.service

import com.taskmanager.application.dto.user.CreateUserDto
import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.UserRole
import com.taskmanager.demo.repository.UserRepository
import com.taskmanager.infrastructure.exception.CustomExceptions // Import correto da sua exceção
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks lateinit var userService: UserService

    @Test
    fun `create deve lancar ResourceConflictException quando username ja existe`() {
        // 1. ARRANGE
        val dto = CreateUserDto(
            username = "joao.duplicado",
            password = "123",
            fullName = "Joao",
            email = "j@j.com",
            role = UserRole.FUNCIONARIO
        )

        // Simula que o banco JÁ encontrou alguém com esse username
        val existingUser = User(
            loginUsername = "joao.duplicado", 
            passwordHash = "hash", 
            role = UserRole.FUNCIONARIO, 
            fullName = "Existente"
        )
        
        // IMPORTANTE: Usando o método correto findByLoginUsername
        `when`(userRepository.findByLoginUsername(dto.username)).thenReturn(existingUser)

        // 2. ACT & ASSERT
        // Verifica se a exceção correta é lançada
        assertThrows(CustomExceptions.ResourceConflictException::class.java) {
            userService.create(dto)
        }
        
        // Garante que NUNCA tentou salvar
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `create deve salvar usuario e hashear senha quando username eh novo`() {
        // 1. ARRANGE
        val dto = CreateUserDto(
            username = "novo.user", 
            password = "senha123", 
            fullName = "Novo", 
            email = "n@n.com", 
            role = UserRole.ADMIN
        )
        
        // Simula que não encontrou ninguém (null) -> Username livre
        `when`(userRepository.findByLoginUsername(dto.username)).thenReturn(null)
        
        // Simula o encoder de senha
        `when`(passwordEncoder.encode(dto.password)).thenReturn("senha_hasheada_segura")
        
        // Simula o salvamento (retorna o objeto salvo com um ID gerado)
        `when`(userRepository.save(any(User::class.java))).thenAnswer { 
            val usuarioSalvo = it.getArgument(0) as User
            // Retorna uma cópia com ID simulado (como o banco faria)
            usuarioSalvo.copy(id = "uuid-gerado-123") 
        }

        // 2. ACT
        val resultDto = userService.create(dto)

        // 3. ASSERT
        assertEquals("uuid-gerado-123", resultDto.id)
        assertEquals("novo.user", resultDto.username)
        
        // Verifica se a senha foi hasheada antes de salvar (não podemos ver a senha no DTO, mas verificamos a chamada)
        verify(passwordEncoder, times(1)).encode("senha123")
        verify(userRepository, times(1)).save(any())
    }
}