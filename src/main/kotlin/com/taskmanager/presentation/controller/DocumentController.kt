package com.taskmanager.presentation.controller

import com.taskmanager.application.dto.document.*
import com.taskmanager.application.service.DocumentService
import com.taskmanager.demo.security.CustomUserDetailsService
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
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.io.IOException

@RestController
@RequestMapping("/api/documents")
class DocumentController(
    private val documentService: DocumentService,
    private val customUserDetailsService: CustomUserDetailsService
) {
    // Helper: Assumimos que o username do principal é o ID do User/Employee
    private fun getEmployeeIdFromPrincipal(principal: UserDetails): String {
        return principal.username // Usamos o username do principal
    }

    // --- Endpoints Básicos de Metadados ---

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE', 'FUNCIONARIO')")
    fun findAll(
        @PageableDefault(size = 20, sort = ["createdAt"]) pageable: Pageable,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) type: String?, // Recebe como String, GenericSpecification compara
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false, defaultValue = "true") isActive: Boolean
    ): ResponseEntity<Page<DocumentDto>> {
        val documentsPage = documentService.findAll(pageable, projectId, type, status, isActive)
        return ResponseEntity.ok(documentsPage)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE', 'FUNCIONARIO')")
    fun findById(@PathVariable id: String): ResponseEntity<DocumentDto> {
        val document = documentService.findById(id)
        return ResponseEntity.ok(document)
    }
    
    // --- Upload e Criação ---

    /**
     * Endpoint: POST /api/documents
     * Função: Cria um novo documento (metadados) e sua primeira versão de arquivo.
     */
    @PostMapping(consumes = ["multipart/form-data"])
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE', 'FUNCIONARIO')")
    fun createAndUpload(
        @RequestPart("data") @Valid dto: CreateDocumentDto,
        @RequestPart("file") file: MultipartFile,
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<UploadVersionResponseDto> {
        if (file.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo é obrigatório.")
        }
        val employeeId = getEmployeeIdFromPrincipal(principal)
        val response = documentService.createAndUpload(dto, file, employeeId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    /**
     * Endpoint: POST /api/documents/{id}/upload
     * Função: Faz upload de uma nova versão do arquivo para um documento existente.
     */
    @PostMapping("/{id}/upload", consumes = ["multipart/form-data"])
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE', 'FUNCIONARIO')")
    fun uploadNewVersion(
        @PathVariable id: String,
        @RequestPart("file") file: MultipartFile,
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<UploadVersionResponseDto> {
        if (file.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo é obrigatório.")
        }
        val employeeId = getEmployeeIdFromPrincipal(principal)
        val response = documentService.uploadNewVersion(id, file, employeeId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // --- Aprovações ---
    
    /**
     * Endpoint: GET /api/documents/{id}/approvals
     * Função: Lista todos os registros de aprovação para este documento.
     */
    @GetMapping("/{id}/approvals")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS', 'LIDER_EQUIPE', 'FUNCIONARIO')")
    fun getApprovals(@PathVariable id: String): ResponseEntity<List<DocumentApprovalDto>> {
        val approvals = documentService.getApprovals(id)
        return ResponseEntity.ok(approvals)
    }

    /**
     * Endpoint: POST /api/documents/{id}/approve
     * Função: Registra a decisão de aprovação ou rejeição por um usuário.
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')")
    fun registerApproval(
        @PathVariable id: String,
        @Valid @RequestBody dto: CreateApprovalRequestDto,
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<DocumentApprovalDto> {
        val employeeId = getEmployeeIdFromPrincipal(principal)
        val approval = documentService.registerApproval(id, dto, employeeId)
        return ResponseEntity.status(HttpStatus.CREATED).body(approval)
    }

    /**
     * Endpoint: PUT /api/documents/{id}
     * Função: Atualiza os metadados do documento (título, tipo, status, etc.).
     * Acesso: ADMIN, GESTOR_OBRAS
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')")
    fun update(@PathVariable id: String, @Valid @RequestBody dto: UpdateDocumentDto): ResponseEntity<DocumentDto> {
        val updatedDocument = documentService.update(id, dto)
        return ResponseEntity.ok(updatedDocument)
    }

    /**
     * Endpoint: DELETE /api/documents/{id}
     * Função: Realiza Soft Delete no documento.
     * Acesso: ADMIN, GESTOR_OBRAS
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_OBRAS')")
    fun delete(
        @PathVariable id: String, 
        @AuthenticationPrincipal principal: UserDetails 
    ): ResponseEntity<Void> {
        val deletedByUsername = getEmployeeIdFromPrincipal(principal) // Reutiliza o helper
        documentService.softDelete(id, deletedByUsername)
        return ResponseEntity.noContent().build()
    }

    // --- Download ---
    // O endpoint de download exigiria a simulação do serviço de storage, 
    // que está fora do escopo do serviço de metadados.
}