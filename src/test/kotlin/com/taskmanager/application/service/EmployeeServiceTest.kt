package com.taskmanager.application.service

import com.taskmanager.application.dto.employee.CreateEmployeeDto
import com.taskmanager.demo.model.Employee
import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.Department
import com.taskmanager.demo.model.enums.EmployeeStatus
import com.taskmanager.demo.model.enums.UserRole
import com.taskmanager.demo.repository.EmployeeRepository
import com.taskmanager.demo.repository.UserRepository
import com.taskmanager.infrastructure.exception.CustomExceptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class EmployeeServiceTest {

    @Mock lateinit var employeeRepository: EmployeeRepository
    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks lateinit var employeeService: EmployeeService

    @Test
    fun `create deve salvar Employee e User quando dados sao validos`() {
        // 1. ARRANGE
        val dto = CreateEmployeeDto(
            username = "maria.santos",
            password = "123",
            fullName = "Maria Santos",
            email = "m@m.com",
            registrationNumber = "REG-001",
            role = UserRole.GESTOR_OBRAS,
            department = Department.ENGENHARIA,
            status = EmployeeStatus.ACTIVE,
            phone = "1199999999",
            hireDate = LocalDate.now()
        )

        // Mocks de validação (não encontrou duplicatas)
        `when`(userRepository.findByLoginUsername(dto.username)).thenReturn(null)
        `when`(employeeRepository.findByRegistrationNumber(dto.registrationNumber)).thenReturn(null)
        
        `when`(passwordEncoder.encode(dto.password)).thenReturn("encoded_pass")

        // Mock do salvamento do User
        `when`(userRepository.save(any(User::class.java))).thenAnswer { 
            val u = it.getArgument(0) as User
            u.copy(id = "user-id-1")
        }

        // Mock do salvamento do Employee
        `when`(employeeRepository.save(any(Employee::class.java))).thenAnswer {
            val e = it.getArgument(0) as Employee
            e.copy(id = "emp-id-1")
        }

        // 2. ACT
        val result = employeeService.create(dto)

        // 3. ASSERT
        assertNotNull(result.id)
        assertEquals("REG-001", result.registrationNumber)
        assertEquals("Maria Santos", result.fullName) // Verifica se pegou do User
        
        // Verifica a cadeia de chamadas
        verify(userRepository).save(any())
        verify(employeeRepository).save(any())
    }

    @Test
    fun `create deve lancar ResourceConflictException se matricula ja existe`() {
        // 1. ARRANGE
        val dto = CreateEmployeeDto(
            username = "novo.user", password = "123", fullName = "Novo", email = null,
            registrationNumber = "REG-DUPLICADO", role = UserRole.FUNCIONARIO, department = Department.RH,
            status = EmployeeStatus.ACTIVE, phone = null, hireDate = null
        )

        // Simula username livre, mas matrícula ocupada
        `when`(userRepository.findByLoginUsername(dto.username)).thenReturn(null)
        
        val existingEmployee = Employee(id = "existing", user = User(loginUsername = "x", passwordHash = "", role = UserRole.ADMIN, fullName = "X"), registrationNumber = "REG-DUPLICADO", department = Department.RH, status = EmployeeStatus.ACTIVE)
        `when`(employeeRepository.findByRegistrationNumber(dto.registrationNumber)).thenReturn(existingEmployee)

        // 2. ACT & ASSERT
        assertThrows(CustomExceptions.ResourceConflictException::class.java) {
            employeeService.create(dto)
        }
        
        verify(employeeRepository, never()).save(any())
    }

    @Test
    fun `softDelete deve desativar tanto o Employee quanto o User associado`() {
        // 1. ARRANGE
        val empId = "emp-1"
        val userId = "user-1"
        
        // Mock do User e Employee existentes e ativos
        val userMock = User(id = userId, loginUsername = "maria", passwordHash = "", role = UserRole.GESTOR_OBRAS, fullName = "Maria")
        userMock.isActive = true
        
        val empMock = Employee(id = empId, user = userMock, registrationNumber = "REG1", department = Department.ENGENHARIA, status = EmployeeStatus.ACTIVE)
        empMock.isActive = true

        `when`(employeeRepository.findById(empId)).thenReturn(Optional.of(empMock))

        // 2. ACT
        employeeService.softDelete(empId, "admin-que-deletou")

        // 3. ASSERT
        // Verifica se ambos foram desativados
        assertFalse(empMock.isActive)
        assertFalse(userMock.isActive)
        
        // Verifica se ambos foram salvos
        verify(employeeRepository).save(empMock)
        verify(userRepository).save(userMock)
    }
}