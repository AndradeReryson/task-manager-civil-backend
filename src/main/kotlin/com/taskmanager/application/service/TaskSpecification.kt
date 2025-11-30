package com.taskmanager.application.service

import com.taskmanager.demo.model.Task
import com.taskmanager.infrastructure.filter.GenericSpecification
import com.taskmanager.infrastructure.filter.SearchCriteria
import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification

class TaskSpecification(private val criteriaList: List<SearchCriteria>) : Specification<Task> {
    override fun toPredicate(root: Root<Task>, query: CriteriaQuery<*>?, builder: CriteriaBuilder): Predicate? {
        val predicates = criteriaList.mapNotNull { GenericSpecification<Task>(it).toPredicate(root, query, builder) }
        return if (predicates.isNotEmpty()) builder.and(*predicates.toTypedArray()) else null
    }
    
    companion object {
        fun activeTasks(): Specification<Task> = Specification { root, _, builder -> builder.isTrue(root.get("isActive")) }
    }
}