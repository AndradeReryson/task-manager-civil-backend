package com.taskmanager.demo.model

import com.taskmanager.demo.model.enums.FinancialType // RECEITA, DESPESA, CUSTO, TRANSFERENCIA
import com.taskmanager.demo.model.enums.FinancialCategory // FOLHA_PAGAMENTO, MATERIAL, SERVICO, VENDA
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "financial")
data class Financial(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(nullable = false)
    var description: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: FinancialType, // Tipo de transação: Receita, Despesa, Custo, etc.

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var category: FinancialCategory, // Categoria: Folha de Pagamento, Compra de Material, Venda de Serviço

    @Column(nullable = false, precision = 15, scale = 2)
    var amount: BigDecimal, // Valor da transação (positivo para receita, negativo para despesa)

    @Column(nullable = false)
    var transactionDate: LocalDate, // Data em que a transação ocorreu

    // Relação Many-to-One com Project (Opcional: A transação pode estar ligada a um projeto específico)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    var project: Project? = null,

    // Relação Many-to-One com Employee (Opcional: O funcionário afetado ou responsável pela transação. Ex: Salário)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = true)
    var employee: Employee? = null,

    @Column(nullable = true)
    var receiptUrl: String? = null, // URL ou caminho para o comprovante/recibo

) : AuditableEntity() // Herda campos de auditoria e soft delete