package com.taskmanager.application.service

import com.taskmanager.application.dto.task.CreateTaskDto
import com.taskmanager.demo.model.Employee
import com.taskmanager.demo.model.Project
import com.taskmanager.demo.model.Task
import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.*
import com.taskmanager.demo.repository.EmployeeRepository
import com.taskmanager.demo.repository.ProjectRepository
import com.taskmanager.demo.repository.TaskRepository
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
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TaskServiceTest {

    @Mock lateinit var taskRepository: TaskRepository
    @Mock lateinit var projectRepository: ProjectRepository
    @Mock lateinit var employeeRepository: EmployeeRepository

    @InjectMocks lateinit var taskService: TaskService

    // Helpers para criar mocks rápidos
    private fun mockEmployee(id: String, username: String): Employee {
        val user = User(id = "u-$id", loginUsername = username, passwordHash = "", role = UserRole.FUNCIONARIO, fullName = "User $username", email = "$username@mail.com")
        return Employee(id = id, user = user, registrationNumber = "R-$id", department = Department.OPERACIONAL, status = EmployeeStatus.ACTIVE)
    }

    private fun mockProject(id: String): Project {
        return Project(id = id, name = "Projeto Teste", status = ProjectStatus.EM_ANDAMENTO)
    }

    @Test
    fun `create deve salvar tarefa com relacionamentos corretos`() {
        // 1. ARRANGE
        val projectId = "proj-1"
        val assigneeId = "emp-joao"
        val reporterUsername = "maria.santos" // Username do token

        val dto = CreateTaskDto(
            title = "Nova Tarefa",
            description = "Fazer algo",
            projectId = projectId,
            assignedToId = assigneeId,
            priority = TaskPriority.ALTA,
            dueDate = LocalDateTime.now().plusDays(5)
        )

        val projectMock = mockProject(projectId)
        val assigneeMock = mockEmployee(assigneeId, "joao")
        val reporterMock = mockEmployee("emp-maria", reporterUsername)

        // Configura mocks
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(projectMock))
        `when`(employeeRepository.findById(assigneeId)).thenReturn(Optional.of(assigneeMock))
        // ATENÇÃO: Aqui usamos o método findByUser_LoginUsername que criamos
        `when`(employeeRepository.findByUser_LoginUsername(reporterUsername)).thenReturn(Optional.of(reporterMock))

        `when`(taskRepository.save(any(Task::class.java))).thenAnswer {
            val t = it.getArgument(0) as Task
            t.copy(id = "task-generated-id")
        }

        // 2. ACT
        val result = taskService.create(dto, reporterUsername)

        // 3. ASSERT
        assertNotNull(result.id)
        assertEquals("Nova Tarefa", result.title)
        assertEquals(projectId, result.projectId)
        assertEquals(assigneeId, result.assignedToId)
        assertEquals(reporterMock.id, result.reporterId) // Verifica se pegou o ID da Maria corretamente
        
        verify(taskRepository).save(any())
    }

    @Test
    fun `create deve lancar erro se Projeto nao existe`() {
        // 1. ARRANGE
        // Omissão de 'assignedToId' causava o erro.
        val dto = CreateTaskDto(
            title = "Task", 
            projectId = "invalido", 
            assignedToId = "dummy-employee-id", // <--- CORREÇÃO: Adicionamos um ID placeholder
            description = "A description" // Garante que a ordem e campos obrigatórios são atendidos
            // Status e Priority usam defaults definidos no DTO
        ) 
        
        // Configuração dos Mocks...
        `when`(projectRepository.findById("invalido")).thenReturn(Optional.empty())

        // 2. ACT & ASSERT
        assertThrows(CustomExceptions.ResourceNotFoundException::class.java) {
            // Agora, o serviço tentará buscar o projeto e falhará (o que é o objetivo do teste).
            taskService.create(dto, "qualquer.user")
        }
        
        verify(taskRepository, never()).save(any())
    }
}