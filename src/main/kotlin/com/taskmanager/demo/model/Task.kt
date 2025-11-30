package com.taskmanager.demo.model

import com.taskmanager.demo.model.enums.TaskPriority // Assumindo enum para prioridade
import com.taskmanager.demo.model.enums.TaskStatus // Assumindo enum para status
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tasks")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(nullable = false)
    var title: String, // Título da tarefa

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    // Relação Many-to-One com Project (Toda tarefa pertence a um projeto)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Project,

    // Relação Many-to-One com Employee (Responsável pela execução)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id", nullable = true)
    var assignedTo: Employee? = null,

    // Relação Many-to-One com Employee (Quem criou a tarefa)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = true)
    var reporter: Employee? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: TaskStatus, // PENDENTE, EM_PROGRESSO, REVISAO, CONCLUIDA, BLOQUEADA

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var priority: TaskPriority, // BAIXA, MEDIA, ALTA, URGENTE

    @Column(nullable = true)
    var dueDate: LocalDateTime? = null, // Prazo final

    @Column(nullable = true)
    var completionDate: LocalDateTime? = null, // Data de conclusão real

    // Relação One-to-Many com Documents (Documentos anexados à tarefa)
    @OneToMany(mappedBy = "task", cascade = [CascadeType.ALL], orphanRemoval = true)
    val documents: MutableSet<Document> = mutableSetOf()
    
) : AuditableEntity() // Herda campos de auditoria e soft delete