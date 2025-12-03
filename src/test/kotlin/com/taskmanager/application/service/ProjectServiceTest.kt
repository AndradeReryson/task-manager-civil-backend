package com.taskmanager.application.service

import com.taskmanager.application.dto.project.CreateProjectDto
import com.taskmanager.demo.model.Employee
import com.taskmanager.demo.model.Project
import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.Department
import com.taskmanager.demo.model.enums.EmployeeStatus
import com.taskmanager.demo.model.enums.ProjectStatus
import com.taskmanager.demo.model.enums.UserRole
import com.taskmanager.demo.repository.EmployeeRepository
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
import org.mockito.kotlin.description
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ProjectServiceTest {

    @Mock lateinit var projectRepository: ProjectRepository
    @Mock lateinit var employeeRepository: EmployeeRepository

    @InjectMocks lateinit var projectService: ProjectService

    @Test
    fun `create deve salvar projeto quando dados sao validos`() {
        // 1. ARRANGE
        val managerId = "emp-123"
        val dto = CreateProjectDto(
            name = "Projeto Alpha",
            description = "Descricao do projeto",
            status = ProjectStatus.PLANEJAMENTO,
            budget = BigDecimal("50000.00"),
            managerId = managerId
        )

        // Mock do Gerente (Employee)
        val userMock = User(id = "user-1", loginUsername = "maria", passwordHash = "", role = UserRole.GESTOR_OBRAS, fullName = "Maria", email = "m@m.com")
        val managerMock = Employee(
            id = managerId, 
            user = userMock, 
            registrationNumber = "REG1", 
            department = Department.ENGENHARIA, 
            status = EmployeeStatus.ACTIVE
        )

        // Comportamentos esperados
        `when`(projectRepository.findByName(dto.name)).thenReturn(null) // Nome não existe (OK)
        `when`(employeeRepository.findById(managerId)).thenReturn(Optional.of(managerMock)) // Gerente existe
        
        // Simula o salvamento
        `when`(projectRepository.save(any(Project::class.java))).thenAnswer { 
            val p = it.getArgument(0) as Project
            p.copy(id = "proj-new-id") 
        }

        // 2. ACT
        val result = projectService.create(dto)

        // 3. ASSERT
        assertNotNull(result.id)
        assertEquals("Projeto Alpha", result.name)
        assertEquals(managerId, result.managerId)
        
        verify(projectRepository).save(any())
    }

    @Test
    fun `create deve lancar ResourceConflictException se nome do projeto ja existe`() {
        // 1. ARRANGE
        val dto = CreateProjectDto(name = "Projeto Duplicado", description = null)
        val existingProject = Project(name = "Projeto Duplicado", status = ProjectStatus.EM_ANDAMENTO)

        `when`(projectRepository.findByName(dto.name)).thenReturn(existingProject)

        // 2. ACT & ASSERT
        assertThrows(CustomExceptions.ResourceConflictException::class.java) {
            projectService.create(dto)
        }
        
        verify(projectRepository, never()).save(any())
    }
    
    @Test
    fun `create deve lancar ResourceNotFoundException se gerente nao existe`() {
        // 1. ARRANGE
        val dto = CreateProjectDto(name = "Novo Projeto", description = null, managerId = "id-inexistente")

        `when`(projectRepository.findByName(dto.name)).thenReturn(null)
        `when`(employeeRepository.findById("id-inexistente")).thenReturn(Optional.empty())

        // 2. ACT & ASSERT
        assertThrows(CustomExceptions.ResourceNotFoundException::class.java) {
            projectService.create(dto)
        }
    }
}