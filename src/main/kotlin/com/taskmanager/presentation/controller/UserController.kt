package com.taskmanager.presentation.controller

import com.taskmanager.application.dto.user.CreateUserDto
import com.taskmanager.application.dto.user.UpdateUserDto
import com.taskmanager.application.dto.user.UserDto
import com.taskmanager.application.service.UserService
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
@RequestMapping("/api/users")
// Toda a classe exige a role ADMIN para acessar qualquer método
@PreAuthorize("hasRole('ADMIN')") 
class UserController(
    private val userService: UserService
) {

    /**
     * Endpoint: GET /api/users
     * Função: Lista todos os usuários (com paginação). Requer ADMIN.
     */
    @GetMapping
    fun findAll(@PageableDefault(size = 20, sort = ["fullName"]) pageable: Pageable): ResponseEntity<Page<UserDto>> {
        val usersPage = userService.findAll(pageable)
        return ResponseEntity.ok(usersPage)
    }

    /**
     * Endpoint: GET /api/users/{id}
     * Função: Busca um usuário específico. Requer ADMIN.
     */
    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): ResponseEntity<UserDto> {
        val user = userService.findById(id)
        return ResponseEntity.ok(user)
    }

    /**
     * Endpoint: POST /api/users
     * Função: Cria um novo usuário. Requer ADMIN.
     */
    @PostMapping
    fun create(@Valid @RequestBody dto: CreateUserDto): ResponseEntity<UserDto> {
        val newUser = userService.create(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser)
    }

    /**
     * Endpoint: PUT /api/users/{id}
     * Função: Atualiza um usuário. Requer ADMIN.
     */
    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @Valid @RequestBody dto: UpdateUserDto): ResponseEntity<UserDto> {
        val updatedUser = userService.update(id, dto)
        return ResponseEntity.ok(updatedUser)
    }

    /**
     * Endpoint: DELETE /api/users/{id}
     * Função: Realiza Soft Delete no usuário. Requer ADMIN.
     * Nota: O username do usuário logado será usado para preencher o campo 'deletedBy'.
     */
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: String, 
        @AuthenticationPrincipal principal: UserDetails // Injeta o usuário logado
    ): ResponseEntity<Void> {
        userService.softDelete(id, principal.username)
        return ResponseEntity.noContent().build()
    }
}