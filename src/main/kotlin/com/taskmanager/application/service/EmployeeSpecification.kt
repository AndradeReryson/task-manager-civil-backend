package com.taskmanager.application.service

import com.taskmanager.demo.model.Employee
import com.taskmanager.infrastructure.filter.GenericSpecification
import com.taskmanager.infrastructure.filter.SearchCriteria
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class EmployeeSpecification(
    private val criteriaList: List<SearchCriteria>
) : Specification<Employee> {

    override fun toPredicate(
        root: Root<Employee>, 
        query: CriteriaQuery<*>?, // Note o '?' para casar com a interface corrigida
        builder: CriteriaBuilder
    ): Predicate? {
        
        // Mapeia cada critério para um Predicado JPA usando nossa GenericSpecification
        val predicates = criteriaList.mapNotNull { criteria ->
            GenericSpecification<Employee>(criteria).toPredicate(root, query, builder)
        }

        // Se houver predicados, combina todos com AND. Se não, retorna null (sem filtro).
        return if (predicates.isNotEmpty()) {
            builder.and(*predicates.toTypedArray())
        } else {
            null
        }
    }
    
    companion object {
        // Filtro estático para trazer apenas funcionários ativos (Soft Delete)
        fun activeEmployees(): Specification<Employee> {
            return Specification { root, _, builder ->
                builder.isTrue(root.get("isActive"))
            }
        }
    }
}