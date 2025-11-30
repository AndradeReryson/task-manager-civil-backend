package com.taskmanager.demo.security // <--- NOVO PACOTE

import com.taskmanager.demo.repository.UserRepository // Repositório a ser criado
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.*
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository // Injetando o Repositório de Usuários
) : UserDetailsService {

    // Método principal exigido pelo Spring Security (usado no Login para buscar pelo username)
    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByLoginUsername(username)
            ?: throw UsernameNotFoundException("Usuário não encontrado com username: $username")
        
        // 🚨 PASSO DE DEBUG: IMPRIMA O HASH LIDO DO BANCO
        println("### HASH LIDO DO BANCO: ${user.passwordHash}")
        println("### TAMANHO DO HASH LIDO: ${user.passwordHash.length}")
        // FIM DO DEBUG

        // Retornamos a entidade User, que implementa UserDetails
        return user 
    }
    
    // Método auxiliar customizado para uso no JwtAuthenticationFilter
    @Throws(UsernameNotFoundException::class)
    fun loadUserByUserId(userId: String): UserDetails {
        val user = userRepository.findById(userId).orElse(null)
            ?: throw UsernameNotFoundException("Usuário não encontrado com ID: $userId")
            
        // Se o usuário estiver inativo (soft delete), Spring Security irá bloquear o acesso
        if (!user.isEnabled) {
            throw UsernameNotFoundException("Usuário inativo: $userId")
        }
        
        return user
    }
}