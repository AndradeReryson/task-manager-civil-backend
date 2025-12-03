package com.taskmanager.infrastructure.exception

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.ZonedDateTime

import org.springframework.security.access.AccessDeniedException

/**
 * Define o formato padronizado de erro JSON que o frontend espera.
 */
data class ErrorResponse(
    val timestamp: ZonedDateTime = ZonedDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val errors: List<Map<String, String>>? = null // Detalhes de validacao de campo
)

@ControllerAdvice
@Hidden
class GlobalExceptionHandler {

    // --- 1. Tratamento para Excecões Customizadas (Service Layer) ---

    /**
     * Trata excecões do nosso dominio (BaseCustomException, ResourceNotFoundException, etc.)
     */
    @ExceptionHandler(BaseCustomException::class)
    fun handleCustomException(ex: BaseCustomException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = ex.status.value(),
            error = ex.status.reasonPhrase,
            message = ex.message,
            path = request.getDescription(false).substringAfter("uri="),
            errors = ex.errors.ifEmpty { null }
        )
        return ResponseEntity(errorResponse, ex.status)
    }

    // --- 2. Tratamento para Erros de Validacao (Controller Layer) ---

    /**
     * Trata excecões lancadas pelo @Valid (Bean Validation)
     * Retorna 400 Bad Request com detalhes de todos os campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val validationErrors = ex.bindingResult.fieldErrors.map { error ->
            mapOf(
                "field" to error.field,
                "message" to (error.defaultMessage ?: "Valor inválido")
            )
        }

        val status = HttpStatus.BAD_REQUEST

        val errorResponse = ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = "Erro de validacao de dados de entrada.",
            path = request.getDescription(false).substringAfter("uri="),
            errors = validationErrors
        )
        return ResponseEntity(errorResponse, status)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.FORBIDDEN // 403

        val errorResponse = ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = "Acesso negado: Você nao tem permissao para realizar esta acao.",
            path = request.getDescription(false).substringAfter("uri="),
            errors = null
        )
        return ResponseEntity(errorResponse, status)
    }
    
    // --- 3. Tratamento para Excecões Genericas (Fallback) ---

    /**
     * Trata todas as outras excecões nao capturadas.
     * Retorna 500 Internal Server Error.
     */
    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        
        // Em producao, nao devemos expor o detalhe do erro.
        // Aqui, incluimos a mensagem para facilitar o debugging inicial.
        val errorMessage = "Ocorreu um erro inesperado no servidor: ${ex.message}" 

        val errorResponse = ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = errorMessage,
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity(errorResponse, status)
    }
}