package com.taskmanager.demo.repository

import com.taskmanager.demo.model.Employee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.Optional

interface EmployeeRepository : JpaRepository<Employee, String>, JpaSpecificationExecutor<Employee> {
    
    // Para garantir a unicidade no cadastro
    fun findByRegistrationNumber(registrationNumber: String): Employee?
    
    fun findByUser_LoginUsername(loginUsername: String): Optional<Employee>
}