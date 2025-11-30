package com.taskmanager.demo.repository

import com.taskmanager.demo.model.Team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface TeamRepository : JpaRepository<Team, String>, JpaSpecificationExecutor<Team> {
    
    // Para garantir a unicidade no cadastro
    fun findByName(name: String): Team?
}