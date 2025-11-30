package com.taskmanager.application.dto.project

import com.taskmanager.demo.model.enums.ProjectStatus
import java.math.BigDecimal
import java.time.LocalDate

data class UpdateProjectDto(
    val name: String? = null,
    val description: String? = null,
    val status: ProjectStatus? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val actualEndDate: LocalDate? = null,
    val budget: BigDecimal? = null,
    val managerId: String? = null,
    val isActive: Boolean? = null
)