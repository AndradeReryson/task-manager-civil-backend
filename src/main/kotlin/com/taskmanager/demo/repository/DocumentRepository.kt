package com.taskmanager.demo.repository

import com.taskmanager.demo.model.Document
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface DocumentRepository : JpaRepository<Document, String>, JpaSpecificationExecutor<Document> {
    
    fun findAllByProjectId(projectId: String): List<Document>
}