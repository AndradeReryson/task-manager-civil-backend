package com.taskmanager.infrastructure.exception

import org.springframework.http.HttpStatus

/**
 * Exceção base customizada para o sistema TaskManager.
 * Estende RuntimeException para evitar a necessidade de declarar 'throws' em todos os métodos.
 */
abstract class BaseCustomException(
    val status: HttpStatus,
    override val message: String,
    val errors: List<Map<String, String>> = emptyList()
) : RuntimeException(message)

/**
 * Exceção para recursos não encontrados (HTTP 404 Not Found).
 * Ex: Usuário com ID X não existe.
 */
open class ResourceNotFoundException(
    resourceName: String,
    fieldName: String,
    fieldValue: Any
) : BaseCustomException(
    HttpStatus.NOT_FOUND,
    "$resourceName não encontrado(a) com $fieldName : '$fieldValue'"
)

/**
 * Exceção para erros de validação e regras de negócio (HTTP 400 Bad Request).
 * Ex: Username já em uso ou dados de entrada inválidos.
 */
open class ValidationException(
    override val message: String,
    validationErrors: List<Map<String, String>> = emptyList()
) : BaseCustomException(
    HttpStatus.BAD_REQUEST,
    message,
    validationErrors
)

/**
 * Exceção para erros de conflito (HTTP 409 Conflict).
 * Ex: Tentativa de criar um recurso duplicado (Username, Matrícula, etc.).
 */
open class ResourceConflictException(
    override val message: String
) : BaseCustomException(
    HttpStatus.CONFLICT,
    message
)

// Classe container para fácil importação, embora não seja estritamente necessário no Kotlin.
object CustomExceptions {
    class ResourceNotFoundException(resourceName: String, fieldName: String, fieldValue: Any) :
        com.taskmanager.infrastructure.exception.ResourceNotFoundException(resourceName, fieldName, fieldValue)
    
    class ValidationException(message: String, validationErrors: List<Map<String, String>> = emptyList()) :
        com.taskmanager.infrastructure.exception.ValidationException(message, validationErrors)
    
    class ResourceConflictException(message: String) :
        com.taskmanager.infrastructure.exception.ResourceConflictException(message)
}