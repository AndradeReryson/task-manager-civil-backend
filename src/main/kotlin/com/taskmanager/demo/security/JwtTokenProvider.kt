package com.taskmanager.demo.security

import com.taskmanager.demo.config.JwtProperties
import com.taskmanager.demo.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Header
import io.jsonwebtoken.Jws
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Component
class JwtTokenProvider(
  private val jwtProperties: JwtProperties // Injeção via construtor
) {

    // Valores injetados do application.yml
    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String
    
    @Value("\${app.jwt.access-expiration-ms}")
    private var jwtAccessExpirationMs: Long = 3600000 // 1 hora

    @Value("\${app.jwt.refresh-expiration-ms}")
    private var jwtRefreshExpirationMs: Long = 604800000 // 7 dias

    private fun key(): Key {
        // Gera a chave a partir da string Base64 definida no application.properties
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))
    }

    /**
     * Constrói a base do JWT builder.
     */
    private fun buildBaseToken(subject: String?, claimsMap: Map<String, Any>, expirationEpochMs: Long): String {
        val currentEpochMs = Clock.System.now().toEpochMilliseconds()
        
        return Jwts.builder()
            // Métodos modernos (não depreciados) aceitam Strings e Longs
            .subject(subject) 
            .issuedAt(Date(currentEpochMs)) // issuedAt ainda precisa de Date ou Instant, mas vamos usar Long
            .expiration(Date(expirationEpochMs)) // expiration ainda precisa de Date ou Instant, mas vamos usar Long
            .claims(claimsMap) // Novo método para adicionar claims
            .signWith(key())
            .compact()
    }

    /**
     * Gera o Access Token.
     */
    fun generateAccessToken(authentication: Authentication): String {
        val user = authentication.principal as User
        val claims: Map<String, Any> = mapOf(
            "username" to user.username,
            "roles" to user.authorities.map { it.authority }
        )

        val currentInstant = Clock.System.now()
        val expirationEpochMs = currentInstant.toEpochMilliseconds() + jwtProperties.accessExpirationMs
        
        // Passamos a data de expiração como Long
        return buildBaseToken(user.id, claims, expirationEpochMs) 
    }

    /**
     * Gera um Refresh Token.
     */
    fun generateRefreshToken(user: User): String {
        val claims: Map<String, Any> = mapOf("type" to "refresh")
        
        val currentInstant = Clock.System.now()
        val expirationEpochMs = currentInstant.toEpochMilliseconds() + jwtProperties.refreshExpirationMs

        // Passamos a data de expiração como Long
        return buildBaseToken(user.id, claims, expirationEpochMs)
    }
    
    /**
     * Gera um novo Access Token a partir de Claims de um Refresh Token.
     */
    fun generateAccessTokenFromRefreshToken(claims: Claims): String {
        val accessClaims: Map<String, Any> = mapOf(
          "username" to (claims["username"] ?: "N/A"), // Default if null
          "roles" to (claims["roles"] ?: listOf<String>())  // Default if null
        )
        return buildBaseToken(claims.subject, accessClaims, jwtAccessExpirationMs)
    }

    /**
     * Obtém o parser configurado para o nosso secret.
     */
    private fun getParser() = Jwts.parser()
        .setSigningKey(key())
        .build()

    /**
     * Obtém o ID do usuário (subject) a partir do JWT.
     */
    fun getUserIdFromJWT(token: String): String {
        val claims = getParser()
            .parseClaimsJws(token)
            .body
        // O ID do usuário foi definido como 'subject' no buildBaseToken
        return claims.subject
    }

    /**
     * Obtém os Claims a partir do JWT (usado para refresh token).
     */
    fun getClaimsFromJWT(token: String): Claims {
        return getParser()
            .parseClaimsJws(token)
            .body
    }

    /**
     * Valida o token.
     */
    fun validateToken(token: String): Boolean {
        return try {
            getParser().parseClaimsJws(token)
            true
        } catch (e: Exception) {
            // Tratamento de exceções específico para ExpiredJwtException, SignatureException, etc.
            // Omitido para brevidade
            false
        }
    }
}