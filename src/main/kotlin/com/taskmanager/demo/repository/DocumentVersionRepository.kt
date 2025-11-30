package com.taskmanager.demo.repository

import com.taskmanager.demo.model.DocumentVersion
import org.springframework.data.jpa.repository.JpaRepository

interface DocumentVersionRepository : JpaRepository<DocumentVersion, String> {
    
    // Encontra a versão mais recente de um documento
    fun findFirstByDocumentIdOrderByVersionNumberDesc(documentId: String): DocumentVersion?
    
    // Encontra uma versão específica
    fun findByDocumentIdAndVersionNumber(documentId: String, versionNumber: Int): DocumentVersion?
}