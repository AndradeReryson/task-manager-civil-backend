package com.taskmanager.application.service

import com.taskmanager.application.dto.document.*
import com.taskmanager.demo.model.Document
import com.taskmanager.demo.model.DocumentApproval
import com.taskmanager.demo.model.DocumentVersion
import com.taskmanager.demo.model.Employee
import com.taskmanager.demo.model.enums.ApprovalStatus
import com.taskmanager.demo.model.enums.DocumentStatus
import com.taskmanager.demo.repository.*
import com.taskmanager.infrastructure.exception.CustomExceptions
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile // Import padrão do Spring para upload
import java.time.LocalDateTime

import com.taskmanager.infrastructure.filter.SearchCriteria 
import org.springframework.data.jpa.domain.Specification

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val versionRepository: DocumentVersionRepository,
    private val approvalRepository: DocumentApprovalRepository,
    private val projectRepository: ProjectRepository,
    private val employeeRepository: EmployeeRepository
) {
    // --- Helpers de Conversão ---

    private fun Document.toDto(): DocumentDto {
      val documentId = this.id ?: throw IllegalStateException("Documento deve ter um ID para ser convertido para DTO.")
      val projectId = this.project.id ?: throw IllegalStateException("Documento deve ter um ID de projeto para ser convertido para DTO.")

      return DocumentDto(
          id = documentId, // Usa a variável local
          title = this.title,
          type = this.type,
          status = this.status,
          projectId = projectId,
          taskId = this.task?.id,
          latestVersionNumber = versionRepository.findFirstByDocumentIdOrderByVersionNumberDesc(documentId)?.versionNumber,
          isActive = this.isActive,
          createdAt = this.createdAt
      )
    }

    private fun DocumentVersion.toDto(): DocumentVersionDto = DocumentVersionDto(
        id = this.id!!,
        versionNumber = this.versionNumber,
        fileName = this.fileName,
        fileSize = this.fileSize,
        mimeType = this.mimeType,
        uploadedByEmployeeId = this.uploadedBy.id!!,
        uploadDate = this.uploadDate
    )

    private fun DocumentApproval.toDto(): DocumentApprovalDto = DocumentApprovalDto(
        id = this.id!!,
        approverEmployeeId = this.approver.id!!,
        requiredRole = this.requiredRole,
        status = this.status,
        comments = this.comments,
        approvalDate = this.approvalDate
    )

    // --- Helpers de Busca ---

    private fun findEmployeeById(id: String): Employee {
        return employeeRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Colaborador", "id", id) }
    }

    private fun findDocumentById(id: String): Document {
        return documentRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Documento", "id", id) }
    }


    // --- CRUD Básico ---

    fun findAll(
        pageable: Pageable,
        projectId: String? = null,
        type: String? = null,
        status: String? = null,
        isActive: Boolean = true
    ): Page<DocumentDto> {
        val criteriaList = mutableListOf<SearchCriteria>()

        projectId?.let { criteriaList.add(SearchCriteria("project.id", ":", it)) }
        type?.let { criteriaList.add(SearchCriteria("type", ":", it)) }
        status?.let { criteriaList.add(SearchCriteria("status", ":", it)) }
        
        criteriaList.add(SearchCriteria("isActive", ":", isActive))

        val spec = DocumentSpecification(criteriaList)
        return documentRepository.findAll(spec, pageable).map { it.toDto() }
    }

    fun findById(id: String): DocumentDto {
        return findDocumentById(id).toDto()
    }

    // --- Criação e Versionamento ---

    /**
     * Cria um novo Documento com a Versão Inicial (0.1) e o arquivo real.
     * @param uploadedByEmployeeId O ID do funcionário que está fazendo o upload.
     */
    @Transactional
    fun createAndUpload(dto: CreateDocumentDto, file: MultipartFile, uploadedByEmployeeId: String): UploadVersionResponseDto {
        // 1. Validações
        val project = projectRepository.findById(dto.projectId)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Projeto", "id", dto.projectId) }
        
        val task = dto.taskId?.let { taskId ->
            project.tasks.find { it.id == taskId }
                ?: throw CustomExceptions.ResourceNotFoundException("Tarefa", "id", taskId)
        }

        // 2. Cria o Documento (Metadados)
        val uploader = findEmployeeById(uploadedByEmployeeId)
        val newDocument = Document(
            title = dto.title,
            type = dto.type,
            status = DocumentStatus.RASCUNHO, // Status inicial
            project = project,
            task = task
        )
        val savedDocument = documentRepository.save(newDocument)

        // 3. Simula o upload e cria a Versão 1
        return saveNewVersion(savedDocument, file, uploader, 1)
    }

    /**
     * Salva uma nova versão para um Documento existente.
     */
    @Transactional
    fun uploadNewVersion(documentId: String, file: MultipartFile, uploadedByEmployeeId: String): UploadVersionResponseDto {
        val document = findDocumentById(documentId)
        val uploader = findEmployeeById(uploadedByEmployeeId)

        // 1. Encontra o próximo número de versão
        val latestVersion = versionRepository.findFirstByDocumentIdOrderByVersionNumberDesc(documentId)
        val nextVersionNumber = (latestVersion?.versionNumber ?: 0) + 1

        // 2. Cria a nova versão e simula o upload
        return saveNewVersion(document, file, uploader, nextVersionNumber)
    }

    private fun saveNewVersion(document: Document, file: MultipartFile, uploader: Employee, versionNumber: Int): UploadVersionResponseDto {
      // Simulação de armazenamento (Em um ambiente real, isto seria S3/Cloud Storage)
      val storagePath = "/documents/${document.id}/v${versionNumber}/${file.originalFilename}"

      val newVersion = DocumentVersion(
          document = document,
          versionNumber = versionNumber,
          fileName = file.originalFilename ?: "arquivo-sem-nome",
          storagePath = storagePath,
          fileSize = file.size,
          mimeType = file.contentType,
          uploadedBy = uploader // <<-- CORREÇÃO: Passando o parâmetro uploader
          // uploadDate é definido na entidade (com valor padrão)
      )
      val savedVersion = versionRepository.save(newVersion)

      // Atualiza o status do documento pai para "EM_REVISAO" após um novo upload
      document.status = DocumentStatus.EM_REVISAO
      documentRepository.save(document)
      
      return UploadVersionResponseDto(document.id!!, savedVersion.toDto())
  }

    // --- Aprovações ---
    
    /**
     * Lista todas as aprovações pendentes/concluídas para um documento.
     */
    fun getApprovals(documentId: String): List<DocumentApprovalDto> {
        findDocumentById(documentId) // Garante que o documento exista
        return approvalRepository.findAllByDocumentId(documentId).map { it.toDto() }
    }

    /**
     * Registra uma tentativa de aprovação/rejeição por um usuário.
     * @param approverEmployeeId O funcionário que está tentando aprovar.
     */
    @Transactional
    fun registerApproval(documentId: String, dto: CreateApprovalRequestDto, approverEmployeeId: String): DocumentApprovalDto {
        val document = findDocumentById(documentId)
        val approver = findEmployeeById(approverEmployeeId)

        // 1. Regra de Negócio: Verificar se o approver tem a ROLE necessária
        val requiredRole = "GESTOR_OBRAS" // Exemplo: Apenas Gestores podem aprovar este tipo de documento
        if (approver.user.role.name != requiredRole) {
            throw CustomExceptions.ValidationException("O colaborador '${approver.user.fullName}' não tem a role necessária ('$requiredRole') para aprovar este documento.")
        }
        
        // 2. Verifica se a aprovação já existe/foi concluída por este usuário (opcional: evitar duplicatas)
        // ...
        
        // 3. Cria o registro de aprovação
        val approval = DocumentApproval(
            document = document,
            approver = approver,
            requiredRole = requiredRole,
            status = dto.status,
            comments = dto.comments,
            approvalDate = LocalDateTime.now()
        )
        val savedApproval = approvalRepository.save(approval)

        // 4. Lógica de atualização do STATUS do Documento:
        // Se a aprovação for positiva, e for a única necessária, atualiza o status do Documento.
        if (dto.status == ApprovalStatus.APROVADO) {
            // Lógica mais complexa (multi-nível) seria necessária aqui.
            // Para simplificar, definimos APROVADO imediatamente.
            document.status = DocumentStatus.APROVADO
            documentRepository.save(document)
        }

        return savedApproval.toDto()
    }

    /**
     * Atualiza os metadados do Documento (não a versão).
     */
    @Transactional
    fun update(id: String, dto: UpdateDocumentDto): DocumentDto {
        val document = findDocumentById(id)
        
        // 1. Atualiza campos simples
        dto.title?.let { document.title = it }
        dto.type?.let { document.type = it }
        dto.status?.let { document.status = it }
        dto.isActive?.let { document.isActive = it }

        // 2. Valida e atualiza a Task (se fornecida)
        if (dto.taskId != null) {
            val project = document.project // Reutiliza o projeto
            val task = project.tasks.find { it.id == dto.taskId }
                ?: throw CustomExceptions.ResourceNotFoundException("Tarefa", "id", dto.taskId)
            document.task = task
        }
        
        val updatedDocument = documentRepository.save(document)
        return updatedDocument.toDto()
    }

    /**
     * Implementa o Soft Delete.
     */
    @Transactional
    fun softDelete(id: String, deletedByUsername: String) {
        val document = findDocumentById(id)
        
        document.isActive = false
        document.deletedAt = LocalDateTime.now()
        document.deletedBy = deletedByUsername
        documentRepository.save(document)
        
        // Regra de Negócio: Opcionalmente, pode ser necessário desativar todas as versões também.
        // As versões não estendem AuditableEntity, mas poderiam ser desativadas manualmente aqui.
        // Para simplificar, confiamos no filtro de acesso ao documento pai.
    }
}