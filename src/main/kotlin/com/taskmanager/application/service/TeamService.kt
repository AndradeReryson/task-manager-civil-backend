package com.taskmanager.application.service

import com.taskmanager.application.dto.team.CreateTeamDto
import com.taskmanager.application.dto.team.TeamDto
import com.taskmanager.application.dto.team.UpdateTeamDto
import com.taskmanager.demo.model.Employee
import com.taskmanager.demo.model.Team
import com.taskmanager.demo.repository.EmployeeRepository
import com.taskmanager.demo.repository.ProjectRepository
import com.taskmanager.demo.repository.TeamRepository
import com.taskmanager.infrastructure.exception.CustomExceptions
import com.taskmanager.infrastructure.filter.GenericSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

import com.taskmanager.infrastructure.filter.SearchCriteria 
import org.springframework.data.jpa.domain.Specification 

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val employeeRepository: EmployeeRepository,
    private val projectRepository: ProjectRepository // Não usado diretamente no CRUD de Team, mas útil para gestão de M:M
) {

    /**
     * Converte a Entidade Team para o DTO de resposta.
     */
    private fun Team.toDto(): TeamDto {
      val teamId = this.id ?: throw IllegalStateException("Time deve ter um ID para ser convertido para DTO.")
      
      return TeamDto(
        id = teamId,
        name = this.name,
        description = this.description,
        leaderId = this.leader?.id,
        // Converte o Set de Employees para uma lista de IDs de String
        memberIds = this.members.map { employee -> 
            // Garante que o ID é tratado como não-nulo (String)
            employee.id ?: throw IllegalStateException("Colaborador (Employee) na equipe ${teamId} deve ter um ID.")
        }.toList(),
        isActive = this.isActive,
        createdAt = this.createdAt
      )
    }

    /**
     * Valida e busca o Employee pelo ID, lançando 404 se não encontrado.
     */
    private fun findEmployeeById(id: String): Employee {
        return employeeRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Colaborador", "id", id) }
    }

    /**
     * Cria uma nova Team.
     */
    @Transactional
    fun create(dto: CreateTeamDto): TeamDto {
        // 1. Valida unicidade do nome
        teamRepository.findByName(dto.name)?.let {
            throw CustomExceptions.ResourceConflictException("Equipe com nome '${dto.name}' já existe.")
        }
        
        // 2. Busca e valida o Líder (se fornecido)
        val leader = dto.leaderId?.let { findEmployeeById(it) }

        // 3. Busca e valida os Membros
        val membersSet = if (dto.memberIds.isNotEmpty()) {
            dto.memberIds.map { findEmployeeById(it) }.toMutableSet()
        } else {
            mutableSetOf()
        }

        // 4. Garante que o líder, se existir, seja um membro da equipe
        if (leader != null && !membersSet.contains(leader)) {
            membersSet.add(leader)
        }

        // 5. Cria e salva a equipe
        val newTeam = Team(
            name = dto.name,
            description = dto.description,
            leader = leader,
            members = membersSet,
            projects = mutableSetOf() // Inicia sem projetos vinculados
        )
        val savedTeam = teamRepository.save(newTeam)

        return savedTeam.toDto()
    }

    /**
     * Busca uma Team pelo ID.
     */
    fun findById(id: String): TeamDto {
        val team = teamRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Equipe", "id", id) }
        return team.toDto()
    }

    /**
     * Atualiza uma Team existente.
     */
    @Transactional
    fun update(id: String, dto: UpdateTeamDto): TeamDto {
        val team = teamRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Equipe", "id", id) }
        
        // 1. Atualiza campos simples
        dto.name?.let { team.name = it }
        dto.description?.let { team.description = it }
        dto.isActive?.let { team.isActive = it }
        
        // 2. Atualiza o Líder
        if (dto.leaderId != null) {
            team.leader = findEmployeeById(dto.leaderId)
        }
        
        // 3. Atualiza Membros (opcional e se fornecido)
        if (dto.memberIds != null) {
            val newMembersSet = dto.memberIds.map { findEmployeeById(it) }.toMutableSet()
            
            // Garante que o novo líder, se existir, esteja na lista de membros
            if (team.leader != null && !newMembersSet.contains(team.leader)) {
                newMembersSet.add(team.leader!!)
            }
            
            // Substitui o conjunto de membros existente
            team.members.clear()
            team.members.addAll(newMembersSet)
        }

        val updatedTeam = teamRepository.save(team)
        return updatedTeam.toDto()
    }

    /**
     * Lista todas as Teams com paginação e ordenação.
     */
    fun findAll(
        pageable: Pageable, 
        name: String? = null,
        leaderId: String? = null,
        isActive: Boolean = true  
    ): Page<TeamDto> {
        
        val criteriaList = mutableListOf<SearchCriteria>()

        // Filtro textual por Nome
        name?.let { criteriaList.add(SearchCriteria("name", "like", it)) }
        
        // Filtro por Líder (Relacionamento)
        leaderId?.let { criteriaList.add(SearchCriteria("leader.id", ":", it)) }

        // Constrói a spec
        var spec: Specification<Team> = TeamSpecification(criteriaList)
        
        // Adiciona o filtro de Ativo/Inativo
        // Usamos o critério manual aqui para garantir consistência com o padrão Generic
        val activeCriteria = SearchCriteria("isActive", ":", isActive)
        val activeSpec = GenericSpecification<Team>(activeCriteria)
        
        spec = spec.and(activeSpec)

        return teamRepository.findAll(spec, pageable).map { it.toDto() }
    }
    
    /**
     * Implementa o Soft Delete.
     */
    @Transactional
    fun softDelete(id: String, deletedByUsername: String) {
        val team = teamRepository.findById(id)
            .orElseThrow { CustomExceptions.ResourceNotFoundException("Equipe", "id", id) }

        team.isActive = false
        team.deletedAt = LocalDateTime.now()
        team.deletedBy = deletedByUsername
        teamRepository.save(team)
    }
}