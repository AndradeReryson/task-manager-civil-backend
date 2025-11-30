package com.taskmanager.demo.model

import com.taskmanager.demo.model.enums.ProjectStatus // Assumindo que criamos este enum
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "projects")
data class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(nullable = false, unique = true)
    var name: String, // Nome do Projeto/Obra

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ProjectStatus, // PLANEJAMENTO, EM_ANDAMENTO, PAUSADO, CONCLUIDO, CANCELADO

    @Column(nullable = true)
    var startDate: LocalDate? = null, // Data de início planejada/real

    @Column(nullable = true)
    var endDate: LocalDate? = null, // Data de término planejada

    @Column(nullable = true)
    var actualEndDate: LocalDate? = null, // Data de término real

    // Orçamento do projeto
    @Column(precision = 15, scale = 2)
    var budget: BigDecimal? = null,

    // Chave estrangeira para o Gerente do Projeto (Employee)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", referencedColumnName = "id", nullable = true)
    var manager: Employee? = null, // O funcionário responsável pela gerência da obra

    // Relações One-to-Many (Mapeamento inverso - mappedBy)
    // As Tarefas e Documentos serão mapeados por suas respectivas entidades.
    // Usamos 'Set' para garantir a unicidade.

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tasks: MutableSet<Task> = mutableSetOf(),

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    val documents: MutableSet<Document> = mutableSetOf(),

    // Relacionamento Many-to-Many com Teams (Equipes)
    // A tabela de junção (Join Table) será definida aqui (project_teams)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "project_teams",
        joinColumns = [JoinColumn(name = "project_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    val teams: MutableSet<Team> = mutableSetOf()

) : AuditableEntity()