package com.taskmanager.application.service

import com.taskmanager.application.dto.document.CreateApprovalRequestDto
import com.taskmanager.application.dto.document.CreateDocumentDto
import com.taskmanager.demo.model.*
import com.taskmanager.demo.model.enums.*
import com.taskmanager.demo.repository.*
import com.taskmanager.infrastructure.exception.CustomExceptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockMultipartFile
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class DocumentServiceTest {

    @Mock lateinit var documentRepository: DocumentRepository
    @Mock lateinit var versionRepository: DocumentVersionRepository
    @Mock lateinit var approvalRepository: DocumentApprovalRepository
    @Mock lateinit var projectRepository: ProjectRepository
    @Mock lateinit var employeeRepository: EmployeeRepository

    @InjectMocks lateinit var documentService: DocumentService

    // Helpers
    private fun mockEmployee(id: String, role: UserRole): Employee {
        val u = User(id = "u-$id", loginUsername = "user$id", passwordHash = "", role = role, fullName = "Func $id")
        return Employee(id = id, user = u, registrationNumber = "R-$id", department = Department.ENGENHARIA, status = EmployeeStatus.ACTIVE)
    }

    @Test
    fun `createAndUpload deve salvar documento e primeira versao`() {
        // 1. ARRANGE
        val projectId = "proj-1"
        val uploaderId = "emp-uploader"
        val dto = CreateDocumentDto(
            title = "Planta Baixa",
            type = DocumentType.PLANO,
            projectId = projectId
        )
        
        // Simula o arquivo enviado
        val file = MockMultipartFile("file", "planta.pdf", "application/pdf", "conteudo".toByteArray())

        val projectMock = Project(id = projectId, name = "Proj", status = ProjectStatus.EM_ANDAMENTO)
        val uploaderMock = mockEmployee(uploaderId, UserRole.FUNCIONARIO)

        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(projectMock))
        `when`(employeeRepository.findById(uploaderId)).thenReturn(Optional.of(uploaderMock))

        // Mock do salvamento do Documento PAI
        `when`(documentRepository.save(any(Document::class.java))).thenAnswer {
            val d = it.getArgument(0) as Document
            d.copy(id = "doc-new-id") // Retorna com ID
        }

        // Mock do salvamento da VERSÃO
        `when`(versionRepository.save(any(DocumentVersion::class.java))).thenAnswer {
            val v = it.getArgument(0) as DocumentVersion
            v.copy(id = "ver-new-id") // Retorna com ID
        }

        // 2. ACT
        val result = documentService.createAndUpload(dto, file, uploaderId)

        // 3. ASSERT
        assertEquals("doc-new-id", result.documentId)
        assertEquals(1, result.version.versionNumber) // Primeira versão deve ser 1
        assertEquals("planta.pdf", result.version.fileName)
        
        verify(documentRepository, times(2)).save(any()) // 1x Criação, 1x Update status
        verify(versionRepository).save(any())
    }

    @Test
    fun `registerApproval deve lancar erro se usuario nao tem permissao`() {
        // 1. ARRANGE
        val docId = "doc-1"
        val approverId = "emp-sem-permissao"
        
        // Documento existente
        val docMock = Document(id = docId, title = "Doc", type = DocumentType.PLANO, status = DocumentStatus.EM_REVISAO, project = Project(id = "p1", name = "", status = ProjectStatus.EM_ANDAMENTO))
        
        // Funcionário comum (NÃO é GESTOR_OBRAS)
        val approverMock = mockEmployee(approverId, UserRole.FUNCIONARIO) 

        val approvalDto = CreateApprovalRequestDto(status = ApprovalStatus.APROVADO, comments = "Ok")

        `when`(documentRepository.findById(docId)).thenReturn(Optional.of(docMock))
        `when`(employeeRepository.findById(approverId)).thenReturn(Optional.of(approverMock))

        // 2. ACT & ASSERT
        // O serviço exige que a Role seja GESTOR_OBRAS (hardcoded no service para simplificação)
        assertThrows(CustomExceptions.ValidationException::class.java) {
            documentService.registerApproval(docId, approvalDto, approverId)
        }
        
        verify(approvalRepository, never()).save(any())
    }
}