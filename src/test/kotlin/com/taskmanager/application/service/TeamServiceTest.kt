package com.taskmanager.application.service

import com.taskmanager.application.dto.team.CreateTeamDto
import com.taskmanager.application.dto.team.UpdateTeamDto
import com.taskmanager.demo.model.Employee
import com.taskmanager.demo.model.Team
import com.taskmanager.demo.model.User
import com.taskmanager.demo.model.enums.Department
import com.taskmanager.demo.model.enums.EmployeeStatus
import com.taskmanager.demo.model.enums.UserRole
import com.taskmanager.demo.repository.EmployeeRepository
import com.taskmanager.demo.repository.ProjectRepository // Injetado no service, precisa mockar
import com.taskmanager.demo.repository.TeamRepository
import com.taskmanager.infrastructure.exception.CustomExceptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TeamServiceTest {

    @Mock lateinit var teamRepository: TeamRepository
    @Mock lateinit var employeeRepository: EmployeeRepository
    @Mock lateinit var projectRepository: ProjectRepository 

    @InjectMocks lateinit var teamService: TeamService

    // Helper
    private fun mockEmployee(id: String): Employee {
        val u = User(id = "u-$id", loginUsername = "user$id", passwordHash = "", role = UserRole.FUNCIONARIO, fullName = "Func $id")
        return Employee(id = id, user = u, registrationNumber = "R-$id", department = Department.OPERACIONAL, status = EmployeeStatus.ACTIVE)
    }

    @Test
    fun `create deve salvar time com lider e membros`() {
        // 1. ARRANGE
        val leaderId = "emp-leader"
        val memberId = "emp-member"
        val dto = CreateTeamDto(
            name = "Equipe Alpha",
            description = "Equipe de teste",
            leaderId = leaderId,
            memberIds = listOf(memberId)
        )

        // Mocks
        val leaderMock = mockEmployee(leaderId)
        val memberMock = mockEmployee(memberId)

        `when`(teamRepository.findByName(dto.name)).thenReturn(null)
        `when`(employeeRepository.findById(leaderId)).thenReturn(Optional.of(leaderMock))
        `when`(employeeRepository.findById(memberId)).thenReturn(Optional.of(memberMock))

        `when`(teamRepository.save(any(Team::class.java))).thenAnswer {
            val t = it.getArgument(0) as Team
            t.copy(id = "team-new-id")
        }

        // 2. ACT
        val result = teamService.create(dto)

        // 3. ASSERT
        assertEquals("Equipe Alpha", result.name)
        assertEquals(leaderId, result.leaderId)
        // O Service tem lógica para adicionar o líder aos membros se não estiver lá
        assertTrue(result.memberIds.contains(leaderId)) 
        assertTrue(result.memberIds.contains(memberId))
        assertEquals(2, result.memberIds.size)
    }

    @Test
    fun `update deve atualizar membros corretamente`() {
        // 1. ARRANGE
        val teamId = "team-1"
        val oldMember = mockEmployee("old-emp")
        val newMember = mockEmployee("new-emp")
        
        val existingTeam = Team(
            id = teamId, 
            name = "Old Name", 
            members = mutableSetOf(oldMember)
        )

        val updateDto = UpdateTeamDto(
            name = "New Name",
            memberIds = listOf(newMember.id!!) // Substitui a lista antiga
        )

        `when`(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam))
        `when`(employeeRepository.findById(newMember.id!!)).thenReturn(Optional.of(newMember))
        
        `when`(teamRepository.save(any(Team::class.java))).thenAnswer { it.getArgument(0) }

        // 2. ACT
        val result = teamService.update(teamId, updateDto)

        // 3. ASSERT
        assertEquals("New Name", result.name)
        assertEquals(1, result.memberIds.size)
        assertEquals(newMember.id, result.memberIds[0]) // Garante que trocou o membro
    }
}