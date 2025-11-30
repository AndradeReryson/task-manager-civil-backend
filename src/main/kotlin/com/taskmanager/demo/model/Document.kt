package com.taskmanager.demo.model

import com.taskmanager.demo.model.enums.DocumentStatus // Status de controle (RASCUNHO, EM_REVISAO, APROVADO, OBSOLETO)
import com.taskmanager.demo.model.enums.DocumentType // Tipo (PLANO, RELATORIO, ORCAMENTO)
import jakarta.persistence.*

@Entity
@Table(name = "documents")
data class Document(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: DocumentType, // Tipo de documento (Plano, Relatório, Orçamento, etc.)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: DocumentStatus,

    // Relação Many-to-One com Project (Todo documento pertence a um projeto)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Project,

    // Relação Many-to-One com Task (Opcional: documento pode estar ligado a uma tarefa específica)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = true)
    var task: Task? = null,

    // Relação One-to-Many para Versionamento
    // O Documento "pai" controla todas as suas versões
    @OneToMany(mappedBy = "document", cascade = [CascadeType.ALL], orphanRemoval = true)
    val versions: MutableSet<DocumentVersion> = mutableSetOf(),

    // Relação One-to-Many para Aprovações
    @OneToMany(mappedBy = "document", cascade = [CascadeType.ALL], orphanRemoval = true)
    val approvals: MutableSet<DocumentApproval> = mutableSetOf()

) : AuditableEntity() // Herda campos de auditoria e soft delete