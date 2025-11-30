package com.taskmanager.demo.repository

import com.taskmanager.demo.model.DocumentApproval
import com.taskmanager.demo.model.enums.ApprovalStatus
import org.springframework.data.jpa.repository.JpaRepository

interface DocumentApprovalRepository : JpaRepository<DocumentApproval, String> {
    
    // Busca todas as aprovações de um documento
    fun findAllByDocumentId(documentId: String): List<DocumentApproval>
    
    // Busca aprovações pendentes para um usuário/role específico
    fun findByDocumentIdAndApproverIdAndStatus(documentId: String, approverId: String, status: ApprovalStatus): DocumentApproval?
}