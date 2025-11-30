package com.taskmanager.demo.model

import com.taskmanager.demo.model.enums.ApprovalStatus // PENDENTE, APROVADO, REJEITADO
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "document_approvals")
data class DocumentApproval(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    var document: Document, // O documento que está sendo avaliado

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    var approver: Employee, // O funcionário responsável pela aprovação

    @Column(nullable = false)
    var requiredRole: String, // Role necessária para aprovar (e.g., "GESTOR_OBRAS")

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ApprovalStatus, // Status da aprovação (PENDENTE, APROVADO, REJEITADO)

    @Column(columnDefinition = "TEXT")
    var comments: String? = null,

    @Column(nullable = true)
    var approvalDate: LocalDateTime? = null // Data em que a decisão foi tomada

    // Não estende AuditableEntity, pois seus campos de auditoria estão implícitos.
)