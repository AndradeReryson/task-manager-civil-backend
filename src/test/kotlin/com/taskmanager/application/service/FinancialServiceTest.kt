package com.taskmanager.application.service

import com.taskmanager.application.dto.financial.CreateFinancialDto
import com.taskmanager.demo.model.Employee
import com.taskmanager.demo.model.Financial
import com.taskmanager.demo.model.Project
import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.*
import com.taskmanager.demo.repository.EmployeeRepository
import com.taskmanager.demo.repository.FinancialRepository
import com.taskmanager.demo.repository.ProjectRepository
import com.taskmanager.infrastructure.exception.CustomExceptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class FinancialServiceTest {

    @Mock lateinit var financialRepository: FinancialRepository
    @Mock lateinit var projectRepository: ProjectRepository
    @Mock lateinit var employeeRepository: EmployeeRepository

    @InjectMocks lateinit var financialService: FinancialService

    @Test
    fun `create deve salvar transacao quando referencias existem`() {
        // 1. ARRANGE
        val projectId = "proj-1"
        val dto = CreateFinancialDto(
            description = "Compra Material",
            type = FinancialType.DESPESA,
            category = FinancialCategory.MATERIAL,
            amount = BigDecimal("150.00"),
            transactionDate = LocalDate.now(),
            projectId = projectId
        )

        val projectMock = Project(id = projectId, name = "Proj", status = ProjectStatus.EM_ANDAMENTO)
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(projectMock))
        
        `when`(financialRepository.save(any(Financial::class.java))).thenAnswer {
            val f = it.getArgument(0) as Financial
            f.copy(id = "fin-id-1")
        }

        // 2. ACT
        val result = financialService.create(dto)

        // 3. ASSERT
        assertNotNull(result.id)
        assertEquals("Compra Material", result.description)
        assertEquals(projectId, result.projectId)
        
        verify(financialRepository).save(any())
    }

    @Test
    fun `create deve lancar erro se projeto informado nao existe`() {
        // 1. ARRANGE
        val dto = CreateFinancialDto(
            description = "Erro", type = FinancialType.RECEITA, category = FinancialCategory.VENDA,
            amount = BigDecimal.TEN, transactionDate = LocalDate.now(),
            projectId = "id-inexistente"
        )

        `when`(projectRepository.findById("id-inexistente")).thenReturn(Optional.empty())

        // 2. ACT & ASSERT
        assertThrows(CustomExceptions.ResourceNotFoundException::class.java) {
            financialService.create(dto)
        }
        
        verify(financialRepository, never()).save(any())
    }
}