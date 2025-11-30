package com.taskmanager.application.service

import com.taskmanager.application.dto.employee.CreateEmployeeDto
import com.taskmanager.application.dto.employee.EmployeeDto
import com.taskmanager.application.dto.employee.UpdateEmployeeDto
import com.taskmanager.demo.model.Employee
import com.taskmanager.demo.model.User
import com.taskmanager.demo.repository.EmployeeRepository
import com.taskmanager.demo.repository.UserRepository
import com.taskmanager.infrastructure.exception.CustomExceptions
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

import com.taskmanager.infrastructure.filter.SearchCriteria 
import org.springframework.data.jpa.domain.Specification

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    /**
     * Converte a Entidade Employee e seu User associado para o DTO de resposta.
     */
    private fun Employee.toDto(): EmployeeDto {
      val employeeId = this.id ?: throw IllegalStateException("Funcionário deve ter um ID para ser convertido para DTO.")
    
      return EmployeeDto(
        id = employeeId,
        registrationNumber = this.registrationNumber,
        fullName = this.user.fullName, // Pega do User
        email = this.user.email,       // Pega do User
        role = this.user.role,         // Pega do User
        department = this.department,
        status = this.status,
        phone = this.phone,
        hireDate = this.hireDate,
        salary = this.salary,
        isActive = this.isActive,
        createdAt = this.createdAt
      )
    }
        

    /**
     * Cria um novo Employee e o User associado em uma única transação.
     */
    @Transactional
    fun create(dto: CreateEmployeeDto): EmployeeDto {
        // 1. Valida unicidade de Username (no User) e Registration Number (no Employee)
        userRepository.findByLoginUsername(dto.username)?.let {
            throw CustomExceptions.ResourceConflictException("Username '${dto.username}' já está em uso.")
        }
        employeeRepository.findByRegistrationNumber(dto.registrationNumber)?.let {
            throw CustomExceptions.ResourceConflictException("Matrícula '${dto.registrationNumber}' já está em uso.")
        }

        // 2. Cria e salva a entidade User primeiro
        val newUser = User(
            loginUsername = dto.username,
            passwordHash = passwordEncoder.encode(dto.password),
            role = dto.role,
            fullName = dto.fullName,
            email = dto.email,
            // Audit fields serão preenchidos automaticamente
        )
        val savedUser = userRepository.save(newUser)

        // 3. Cria e salva a entidade Employee
        val newEmployee = Employee(
            user = savedUser, // Faz a ligação 1:1
            registrationNumber = dto.registrationNumber,
            department = dto.department,
            status = dto.status,
            phone = dto.phone,
            hireDate = dto.hireDate,
            salary = null // Salário pode ser adicionado em um update posterior
        )
        val savedEmployee = employeeRepository.save(newEmployee)

        return savedEmployee.toDto()
    }

    /**
     * Busca um Employee pelo ID.
     */
    fun findById(id: String): EmployeeDto {
        val employee = employeeRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Colaborador", "id", id) }
        return employee.toDto()
    }

    /**
     * Atualiza um Employee e seu User associado.
     */
    @Transactional
    fun update(id: String, dto: UpdateEmployeeDto): EmployeeDto {
        val employee = employeeRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Colaborador", "id", id) }

        // 1. Atualiza a entidade User (que está lazily loaded)
        val user = employee.user
        dto.fullName?.let { user.fullName = it }
        dto.email?.let { user.email = it }
        dto.role?.let { user.role = it }

        // Se o isActive for alterado no Employee, refletir no User
        dto.isActive?.let { 
            employee.isActive = it
            user.isActive = it 
            if (!it) {
                employee.deletedAt = LocalDateTime.now()
                user.deletedAt = LocalDateTime.now()
                // Nota: deletedBy precisa ser injetado/passado, mas simplificamos por enquanto.
            }
        }
        userRepository.save(user)
        
        // 2. Atualiza a entidade Employee
        dto.registrationNumber?.let { employee.registrationNumber = it }
        dto.department?.let { employee.department = it }
        dto.status?.let { employee.status = it }
        dto.phone?.let { employee.phone = it }
        dto.salary?.let { employee.salary = it }

        val updatedEmployee = employeeRepository.save(employee)
        return updatedEmployee.toDto()
    }

    /**
     * Lista todos os Employees com paginação, ordenação e filtros dinâmicos.
     */
    fun findAll(
        pageable: Pageable, 
        department: String? = null, 
        status: String? = null, 
        search: String? = null,
        isActive: Boolean = true
    ): Page<EmployeeDto> {
        val criteriaList = mutableListOf<SearchCriteria>()

        department?.let { criteriaList.add(SearchCriteria("department", ":", it)) }
        status?.let { criteriaList.add(SearchCriteria("status", ":", it)) }
        search?.let { criteriaList.add(SearchCriteria("user.fullName", "like", it)) }
        
        // 🚨 ALTERAÇÃO: Filtro dinâmico
        criteriaList.add(SearchCriteria("isActive", ":", isActive))

        val spec = EmployeeSpecification(criteriaList)
        return employeeRepository.findAll(spec, pageable).map { it.toDto() }
    }
    
    /**
     * Implementa o Soft Delete. Desativa o Employee e o User associado.
     */
    @Transactional
    fun softDelete(id: String, deletedByUsername: String) {
        val employee = employeeRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Colaborador", "id", id) }

        // 1. Aplica o Soft Delete no Employee
        employee.isActive = false
        employee.deletedAt = LocalDateTime.now()
        employee.deletedBy = deletedByUsername
        employeeRepository.save(employee)

        // 2. Aplica o Soft Delete no User associado
        val user = employee.user
        user.isActive = false
        user.deletedAt = LocalDateTime.now()
        user.deletedBy = deletedByUsername
        userRepository.save(user)
    }
}