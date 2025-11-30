package com.taskmanager.demo.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "document_versions")
data class DocumentVersion(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(nullable = false)
    var versionNumber: Int, // 1, 2, 3, etc.

    @Column(nullable = false)
    var fileName: String, // Nome do arquivo original

    @Column(nullable = false)
    var storagePath: String, // O caminho real do arquivo no S3 ou no disco

    @Column(nullable = false)
    var fileSize: Long, // Tamanho do arquivo em bytes

    @Column(nullable = true)
    var mimeType: String? = null, // Tipo MIME (e.g., application/pdf)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    var document: Document, // O documento "pai" desta versão

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    var uploadedBy: Employee, // Quem fez o upload desta versão

    @Column(nullable = false)
    var uploadDate: LocalDateTime = LocalDateTime.now()

    // Não estende AuditableEntity, pois seus campos de upload já são a própria auditoria.
)