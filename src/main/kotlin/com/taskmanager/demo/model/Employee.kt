package com.taskmanager.demo.model

import com.taskmanager.demo.model.enums.Department
import com.taskmanager.demo.model.enums.EmployeeStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "employees")
data class Employee(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    // Relação 1:1 com User
    // Usa uma chave estrangeira para a tabela users
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    val user: User,

    @Column(nullable = false, unique = true)
    var registrationNumber: String, // Matrícula ou número de registro interno

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var department: Department, // ENGENHARIA, COMPRAS, FINANCEIRO, etc.

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: EmployeeStatus, // ACTIVE, ON_LEAVE, TERMINATED

    @Column(nullable = true)
    var phone: String? = null,

    @Column(nullable = true)
    var hireDate: LocalDate? = null, // Data de contratação

    @Column(precision = 10, scale = 2)
    var salary: BigDecimal? = null, // Salário (informação financeira)

    // O relacionamento com Teams (many-to-many) será adicionado depois.

) : AuditableEntity() // Herda campos de auditoria e soft delete