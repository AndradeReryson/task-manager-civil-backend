package com.taskmanager.application.service

import com.taskmanager.demo.model.Document
import com.taskmanager.infrastructure.filter.GenericSpecification
import com.taskmanager.infrastructure.filter.SearchCriteria
import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification

class DocumentSpecification(private val criteriaList: List<SearchCriteria>) : Specification<Document> {
    override fun toPredicate(root: Root<Document>, query: CriteriaQuery<*>?, builder: CriteriaBuilder): Predicate? {
        val predicates = criteriaList.mapNotNull { GenericSpecification<Document>(it).toPredicate(root, query, builder) }
        return if (predicates.isNotEmpty()) builder.and(*predicates.toTypedArray()) else null
    }
    
    companion object {
        fun activeDocuments(): Specification<Document> = Specification { root, _, builder -> builder.isTrue(root.get("isActive")) }
    }
}