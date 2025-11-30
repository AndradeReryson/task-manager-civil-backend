package com.taskmanager.application.service

import com.taskmanager.demo.model.Project
import com.taskmanager.infrastructure.filter.GenericSpecification
import com.taskmanager.infrastructure.filter.SearchCriteria
import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification

class ProjectSpecification(private val criteriaList: List<SearchCriteria>) : Specification<Project> {
    override fun toPredicate(root: Root<Project>, query: CriteriaQuery<*>?, builder: CriteriaBuilder): Predicate? {
        val predicates = criteriaList.mapNotNull { GenericSpecification<Project>(it).toPredicate(root, query, builder) }
        return if (predicates.isNotEmpty()) builder.and(*predicates.toTypedArray()) else null
    }
    
    companion object {
        fun activeProjects(): Specification<Project> = Specification { root, _, builder -> builder.isTrue(root.get("isActive")) }
    }
}