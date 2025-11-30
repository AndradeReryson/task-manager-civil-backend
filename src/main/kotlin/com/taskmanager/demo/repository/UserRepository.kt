package com.taskmanager.demo.repository

import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface UserRepository : JpaRepository<User, String> {
    
    fun findByLoginUsername(loginUsername: String): User?
    
    // Método customizado para a regra de negócio do Soft Delete
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = ?1 AND u.isActive = true")
    fun countActiveAdmins(role: UserRole = UserRole.ADMIN): Long
    
}