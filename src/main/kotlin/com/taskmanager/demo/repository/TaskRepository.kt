package com.taskmanager.demo.repository

import com.taskmanager.demo.model.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface TaskRepository : JpaRepository<Task, String>, JpaSpecificationExecutor<Task> {
    
    // Método para buscar tarefas por projeto (útil para listas de projeto)
    fun findAllByProjectId(projectId: String): List<Task>
    
    // Outros métodos de busca por status, assignedTo, etc., podem ser adicionados aqui
}