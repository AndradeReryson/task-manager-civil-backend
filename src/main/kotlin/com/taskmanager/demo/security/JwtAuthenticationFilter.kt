package com.taskmanager.demo.security

import com.taskmanager.demo.security.JwtTokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val customUserDetailsService: CustomUserDetailsService // Usaremos este service (a ser criado) para carregar o User
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                // 1. Obtém o ID do usuário
                val userId = jwtTokenProvider.getUserIdFromJWT(jwt)
                
                // 2. Carrega os detalhes do usuário (User)
                val userDetails = customUserDetailsService.loadUserByUserId(userId)
                
                // 3. Cria o objeto de autenticação do Spring Security
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, // A senha não é necessária após a autenticação
                    userDetails.authorities
                )
                
                // 4. Adiciona detalhes da requisição (IP, sessão)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                
                // 5. Define a autenticação no contexto de segurança (usuário está logado para esta requisição)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            // Logar o erro (e.g., Token Expirado, Token Inválido, Usuário não encontrado)
            // Para brevidade, omitimos o logging detalhado aqui.
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Extrai o JWT do cabeçalho Authorization: Bearer <token>
     */
    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        // Verifica se o cabeçalho existe e começa com "Bearer "
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        return null
    }
}