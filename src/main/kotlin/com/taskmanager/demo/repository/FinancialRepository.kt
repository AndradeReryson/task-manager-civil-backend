package com.taskmanager.demo.repository

import com.taskmanager.demo.model.Financial
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface FinancialRepository : JpaRepository<Financial, String>, JpaSpecificationExecutor<Financial> {
    
    // Métodos úteis para relatórios e filtros
    fun findAllByProjectId(projectId: String): List<Financial>
    
    fun findAllByEmployeeId(employeeId: String): List<Financial>
    
    // Podemos adicionar métodos para somar valores por categoria/projeto futuramente
}