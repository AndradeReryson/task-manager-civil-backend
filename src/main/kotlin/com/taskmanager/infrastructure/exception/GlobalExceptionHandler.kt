package com.taskmanager.infrastructure.exception

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.ZonedDateTime

/**
 * Define o formato padronizado de erro JSON que o frontend espera.
 */
data class ErrorResponse(
    val timestamp: ZonedDateTime = ZonedDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val errors: List<Map<String, String>>? = null // Detalhes de validação de campo
)

@ControllerAdvice
@Hidden
class GlobalExceptionHandler {

    // --- 1. Tratamento para Exceções Customizadas (Service Layer) ---

    /**
     * Trata exceções do nosso domínio (BaseCustomException, ResourceNotFoundException, etc.)
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

    // --- 2. Tratamento para Erros de Validação (Controller Layer) ---

    /**
     * Trata exceções lançadas pelo @Valid (Bean Validation)
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
            message = "Erro de validação de dados de entrada.",
            path = request.getDescription(false).substringAfter("uri="),
            errors = validationErrors
        )
        return ResponseEntity(errorResponse, status)
    }
    
    // --- 3. Tratamento para Exceções Genéricas (Fallback) ---

    /**
     * Trata todas as outras exceções não capturadas.
     * Retorna 500 Internal Server Error.
     */
    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        
        // Em produção, não devemos expor o detalhe do erro.
        // Aqui, incluímos a mensagem para facilitar o debugging inicial.
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