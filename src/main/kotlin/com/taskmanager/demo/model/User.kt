package com.taskmanager.demo.model

import com.taskmanager.demo.model.enums.UserRole // Assumindo que UserRole é movido para o pacote application.dto
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

// Classe base para campos de auditoria e soft delete
@MappedSuperclass
abstract class AuditableEntity {
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    var createdBy: String? = null
    var updatedBy: String? = null
    
    // Soft Delete fields
    var isActive: Boolean = true
    var deletedAt: LocalDateTime? = null
    var deletedBy: String? = null

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Usaremos UUIDs como chaves primárias
    val id: String? = null,

    @Column(nullable = false, unique = true)
    val loginUsername: String, // Usado para login (e.g., email ou matrícula)

    @Column(nullable = false, length = 255, columnDefinition = "TEXT")
    var passwordHash: String, // Senha hasheada com BCrypt

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var role: UserRole, // ADMIN, GESTOR_OBRAS, LIDER_EQUIPE, FUNCIONARIO

    @Column(nullable = false)
    var fullName: String,

    @Column(nullable = true, unique = true)
    var email: String? = null, // Email de contato
    
    // Relação 1:1 com a entidade Employee
    // Será preenchida quando um User for associado a um Employee
    // Usamos mappedBy no lado proprietário (User é proprietário no db? Não, Employee tem a FK)
    // Se a FK estiver em Employee, essa propriedade será lida como Employee.user
    // Por enquanto, deixaremos de lado a anotação OneToOne aqui para evitar ciclos no modelagem.

) : AuditableEntity(), UserDetails {
    
    // Implementação de UserDetails para Spring Security
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableListOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun isAccountNonExpired(): Boolean = isActive // Poderíamos adicionar um campo de expiração, mas isActive é suficiente por enquanto

    override fun isAccountNonLocked(): Boolean = isActive // Poderíamos adicionar um campo de bloqueio, mas isActive é suficiente por enquanto

    override fun isCredentialsNonExpired(): Boolean = isActive // Tokens não expiram, apenas o JWT

    override fun isEnabled(): Boolean = isActive // O usuário deve estar ativo para logar

    // Implementação de getPassword
    override fun getPassword(): String = passwordHash

    // Implementação de getUsername (usamos o field para evitar o clash)
    override fun getUsername(): String = loginUsername
}