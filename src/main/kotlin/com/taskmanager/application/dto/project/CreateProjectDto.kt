package com.taskmanager.application.dto.project

import com.taskmanager.demo.model.enums.ProjectStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate

data class CreateProjectDto(
    @field:NotBlank(message = "Nome do projeto é obrigatório")
    @field:Size(max = 255)
    val name: String,

    val description: String?,

    // Status inicial padrão (pode ser forçado para PLANEJAMENTO na service)
    val status: ProjectStatus = ProjectStatus.PLANEJAMENTO, 

    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    
    val budget: BigDecimal? = null,

    // ID do funcionário que será o Gerente da Obra
    val managerId: String? = null 
)