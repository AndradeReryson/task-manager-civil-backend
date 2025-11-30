package com.taskmanager.demo.repository

import com.taskmanager.demo.model.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ProjectRepository : JpaRepository<Project, String>, JpaSpecificationExecutor<Project> {
    
    // Método para garantir a unicidade no cadastro
    fun findByName(name: String): Project?
}