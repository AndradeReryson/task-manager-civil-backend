package com.taskmanager.application.service

import com.taskmanager.demo.model.Financial
import com.taskmanager.infrastructure.filter.GenericSpecification
import com.taskmanager.infrastructure.filter.SearchCriteria
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class FinancialSpecification(
    private val criteriaList: List<SearchCriteria>
) : Specification<Financial> {

    override fun toPredicate(root: Root<Financial>, query: CriteriaQuery<*>?, builder: CriteriaBuilder): Predicate? {
        val predicates = criteriaList.mapNotNull { GenericSpecification<Financial>(it).toPredicate(root, query, builder) }
        return if (predicates.isNotEmpty()) builder.and(*predicates.toTypedArray()) else null
    }
    
    companion object {
        // Filtro para trazer apenas registros ativos
        fun activeFinancials(): Specification<Financial> = Specification { root, _, builder -> 
            builder.isTrue(root.get("isActive")) 
        }
    }
}