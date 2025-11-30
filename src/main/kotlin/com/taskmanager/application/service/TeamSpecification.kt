package com.taskmanager.application.service

import com.taskmanager.demo.model.Team
import com.taskmanager.infrastructure.filter.GenericSpecification
import com.taskmanager.infrastructure.filter.SearchCriteria
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class TeamSpecification(
    private val criteriaList: List<SearchCriteria>
) : Specification<Team> {

    override fun toPredicate(root: Root<Team>, query: CriteriaQuery<*>?, builder: CriteriaBuilder): Predicate? {
        val predicates = criteriaList.mapNotNull { GenericSpecification<Team>(it).toPredicate(root, query, builder) }
        return if (predicates.isNotEmpty()) builder.and(*predicates.toTypedArray()) else null
    }
    
    companion object {
        // Regra base: filtrar pelo status ativo/inativo
        fun activeTeams(isActive: Boolean): Specification<Team> {
            return Specification { root, _, builder -> 
                builder.equal(root.get<Boolean>("isActive"), isActive)
            }
        }
    }
}