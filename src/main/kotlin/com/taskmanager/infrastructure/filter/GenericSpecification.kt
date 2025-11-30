package com.taskmanager.infrastructure.filter

import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification
import java.util.Locale

// T é a entidade que estamos consultando (Employee, Project, Task, etc.)
class GenericSpecification<T>(
    private val criteria: SearchCriteria
) : Specification<T> {

    // Método principal que constrói o predicado WHERE
    override fun toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>?, 
        builder: CriteriaBuilder
    ): Predicate? {
        
        // Se o valor for nulo, ignoramos este critério retornando null
        val valueStr = criteria.value?.toString()?.lowercase(Locale.ROOT) ?: return null

        // Obtemos o path já tipado como String para facilitar as comparações de texto
        val path = getPath(root, criteria.key)

        return when (criteria.operation.lowercase(Locale.ROOT)) {
            ">", "gt" -> builder.greaterThanOrEqualTo(path, valueStr)
            "<", "lt" -> builder.lessThanOrEqualTo(path, valueStr)
            
            // Operação de Igualdade (padrão)
            ":", "=" -> {
                if (path.javaType == String::class.java) {
                    // Para strings, faz LIKE case-insensitive
                    builder.like(builder.lower(path), "%$valueStr%")
                } else {
                    // Para igualdade exata, usamos o valor original (Any?) e não a String lowercase
                    // Mas precisamos do path genérico (Path<*>) ou Object para comparar com Any
                    // Aqui, simplificamos mantendo a lógica de String ou tentando comparar o valor original
                    builder.equal(path, criteria.value)
                }
            }
            // LIKE Case-insensitive
            "like" -> builder.like(builder.lower(path), "%$valueStr%")
            
            else -> null // Operação desconhecida
        }
    }
    
    /**
     * Resolve caminhos aninhados como "user.fullName".
     * Retorna um Path<String> assumindo que faremos comparações de texto.
     */
    @Suppress("UNCHECKED_CAST") 
    private fun getPath(root: Root<T>, key: String): Path<String> {
        // CORREÇÃO 5: Tipagem explícita no fold e no .get<Any>
        val path: Path<*> = key.split(".").fold(root as Path<*>) { currentPath, segment ->
            // O Kotlin precisa saber o tipo de retorno de .get(). Usamos <Any> para ser genérico.
            currentPath.get<Any>(segment)
        }
        
        return path as Path<String>
    }
}