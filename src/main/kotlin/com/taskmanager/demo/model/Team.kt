package com.taskmanager.demo.model

import jakarta.persistence.*

@Entity
@Table(name = "teams")
data class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(nullable = false, unique = true)
    var name: String, // Nome da equipe (e.g., "Equipe Estruturas 1", "Equipe Acabamento")

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    // Chave estrangeira para o Líder da Equipe (Employee)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", referencedColumnName = "id", nullable = true)
    var leader: Employee? = null, // O funcionário que é o líder desta equipe

    // Relacionamento Many-to-Many com Employees (Membros da Equipe)
    // Tabela de junção: employee_teams
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "employee_teams",
        joinColumns = [JoinColumn(name = "team_id")],
        inverseJoinColumns = [JoinColumn(name = "employee_id")]
    )
    val members: MutableSet<Employee> = mutableSetOf(),

    // Relacionamento Many-to-Many com Projects (Projetos que a equipe está alocada)
    // Mapeamento inverso (mappedBy) para o relacionamento definido em Project.kt
    @ManyToMany(mappedBy = "teams", fetch = FetchType.LAZY)
    val projects: MutableSet<Project> = mutableSetOf()

) : AuditableEntity() // Herda campos de auditoria e soft delete