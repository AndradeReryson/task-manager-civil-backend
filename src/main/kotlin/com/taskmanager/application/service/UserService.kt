package com.taskmanager.application.service

import com.taskmanager.application.dto.user.CreateUserDto
import com.taskmanager.application.dto.user.UpdateUserDto
import com.taskmanager.application.dto.user.UserDto
import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.UserRole
import com.taskmanager.demo.repository.UserRepository
import com.taskmanager.infrastructure.exception.CustomExceptions
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    /**
     * Converte a entidade User para o DTO de resposta.
     */
    private fun User.toDto(): UserDto {
      val userId = this.id ?: throw IllegalStateException("Usuário deve ter um ID para ser convertido para DTO.")

      return UserDto(
          id = userId,
          username = this.username,
          fullName = this.fullName,
          email = this.email,
          role = this.role,
          isActive = this.isActive,
          createdAt = this.createdAt
      )
    }

    /**
     * Busca um usuário pelo ID.
     */
    fun findById(id: String): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Usuário", "id", id) }
        return user.toDto()
    }

    /**
     * Cria um novo usuário (usado pelo ADMIN).
     */
    @Transactional
    fun create(dto: CreateUserDto): UserDto {
        // Usa ResourceConflictException para falhas de unicidade
        if (userRepository.findByLoginUsername(dto.username) != null) {
            throw CustomExceptions.ResourceConflictException("Username '${dto.username}' já está em uso.")
        }
        
        val newUser = User(
            loginUsername = dto.username,
            passwordHash = passwordEncoder.encode(dto.password),
            role = dto.role,
            fullName = dto.fullName,
            email = dto.email
        )
        val savedUser = userRepository.save(newUser)
        return savedUser.toDto()
    }

    /**
     * Atualiza um usuário existente.
     */
    @Transactional
    fun update(id: String, dto: UpdateUserDto): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Usuário", "id", id) }

        // A lógica de username único deve ser verificada se o DTO permitir a atualização
        // Neste DTO (UpdateUserDto), não permitimos a troca de username para simplificar.

        dto.fullName?.let { user.fullName = it }
        dto.email?.let { user.email = it }
        dto.role?.let { user.role = it }
        dto.isActive?.let { user.isActive = it }
        
        if (dto.newPassword != null) {
            // Usa ValidationException para senhas que não atendem aos requisitos mínimos
            if (dto.newPassword.length < 6) { 
                throw CustomExceptions.ValidationException("A nova senha deve ter pelo menos 6 caracteres.")
            }
            user.passwordHash = passwordEncoder.encode(dto.newPassword)
        }
        
        val updatedUser = userRepository.save(user)
        return updatedUser.toDto()
    }

    /**
     * Lista usuários com paginação e ordenação (considerando o Soft Delete ativo).
     */
    fun findAll(pageable: Pageable): Page<UserDto> {
        // Assumimos que o filtro de Soft Delete (isActive = true) está configurado globalmente no Hibernate.
        return userRepository.findAll(pageable).map { it.toDto() }
    }
    
    /**
     * Implementação do Soft Delete (desativação).
     */
    @Transactional
    fun softDelete(id: String, deletedByUsername: String) {
        val user = userRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Usuário", "id", id) }

        // Regra de Negócio: Não é possível desativar o último ADMIN ativo.
        if (user.role == UserRole.ADMIN && userRepository.countActiveAdmins() <= 1) {
            throw CustomExceptions.ValidationException("Não é possível desativar o último usuário ADMIN ativo.")
        }

        user.isActive = false
        user.deletedAt = LocalDateTime.now()
        user.deletedBy = deletedByUsername
        userRepository.save(user)
    }
}