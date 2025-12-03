package com.taskmanager.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.taskmanager.application.dto.auth.LoginRequestDto
import com.taskmanager.application.dto.employee.CreateEmployeeDto
import com.taskmanager.application.dto.project.CreateProjectDto
import com.taskmanager.application.dto.task.CreateTaskDto
import com.taskmanager.demo.model.enums.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

import com.taskmanager.demo.DemoApplication

import com.taskmanager.demo.model.User 
import com.taskmanager.demo.model.enums.UserRole  
import com.taskmanager.demo.repository.UserRepository 
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootTest(classes = [DemoApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateTaskScenarioTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var passwordEncoder: PasswordEncoder

    // Dados compartilhados entre os testes
    private var tokenAdmin: String = ""
    private var tokenGestor: String = ""     // Maria (Tem permissao)
    private var tokenFuncionario: String = "" // Joao (Nao tem permissao)
    
    private var idGestor: String = ""
    private var idFuncionario: String = ""
    private var idProjetoAtivo: String = ""
    private var idProjetoConcluido: String = ""

    /**
     * SETUP: Criacao do Cenário Base (Atores e Projetos)
     * Executa antes de todos os testes para popular o banco H2.
     */
    @BeforeAll
    fun setupCenario() {
        // CRIAcaO MANUAL DO ADMIN NO BANCO H2
        val adminUser = User(
            loginUsername = "admin",
            passwordHash = passwordEncoder.encode("password"), // Hash correto
            fullName = "Admin Teste",
            email = "admin@teste.com",
            role = UserRole.ADMIN,
        )
        userRepository.save(adminUser)

        // 1. Obter Token Admin (O admin já existe pelo data.sql ou seed inicial)
        tokenAdmin = loginAndGetToken("admin", "password")

        // 2. Criar Gestora (Maria) e pegar Token
        idGestor = createEmployee(tokenAdmin, "maria.test", "GESTOR_OBRAS")
        tokenGestor = loginAndGetToken("maria.test", "password123")

        // 3. Criar Funcionário (Joao) e pegar Token
        idFuncionario = createEmployee(tokenAdmin, "joao.test", "FUNCIONARIO")
        tokenFuncionario = loginAndGetToken("joao.test", "password123")

        // 4. Criar Projeto ATIVO (Alpha)
        idProjetoAtivo = createProject(tokenGestor, "Projeto Alpha", ProjectStatus.EM_ANDAMENTO)

        // 5. Criar Projeto CONCLUiDO (Beta) - Para teste de falha
        idProjetoConcluido = createProject(tokenGestor, "Projeto Beta", ProjectStatus.CONCLUIDO)
    }

    // --- TESTES DOS CASOS DE USO (CT01 a CT08) ---

    @Test
    @Order(1)
    @DisplayName("CT01: Fluxo Principal - Criar Tarefa com Sucesso")
    fun `CT01 - Fluxo Principal (Sucesso Completo)`() {
        val dto = CreateTaskDto(
            title = "Fundacao Torre A",
            description = "Escavacao e concretagem da base conforme planta.",
            projectId = idProjetoAtivo,
            assignedToId = idFuncionario, // Joao vai fazer
            priority = TaskPriority.ALTA,
            dueDate = LocalDateTime.now().plusDays(15),
            status = TaskStatus.PENDENTE
        )

        mockMvc.perform(post("/api/tasks")
            .header("Authorization", "Bearer $tokenGestor") // Maria cria
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("Fundacao Torre A"))
            .andExpect(jsonPath("$.status").value("PENDENTE"))
            .andExpect(jsonPath("$.id").exists())
    }

    @Test
    @Order(2)
    @DisplayName("CT02: Validacao - Tarefa com Titulo Vazio deve falhar")
    fun `CT02 - Excecao Titulo Vazio (Validacao)`() {
        val dto = CreateTaskDto(
            title = "", // INVÁLIDO (Vazio)
            description = "Descricao válida",
            projectId = idProjetoAtivo,
            assignedToId = idFuncionario
        )

        mockMvc.perform(post("/api/tasks")
            .header("Authorization", "Bearer $tokenGestor")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest) // 400
            // O GlobalExceptionHandler retorna erros de validacao
            .andExpect(jsonPath("$.message").value("Erro de validacao de dados de entrada."))
    }

    @Test
    @Order(3)
    @DisplayName("CT03: Valor Limite - Tarefa com Titulo curto deve falhar")
    fun `CT03 - Excecao Titulo Curto (Valor Limite)`() {
        val dto = CreateTaskDto(
            title = "Oi", // INVÁLIDO (< 3 caracteres)
            description = "Descricao válida",
            projectId = idProjetoAtivo,
            assignedToId = idFuncionario
        )

        mockMvc.perform(post("/api/tasks")
            .header("Authorization", "Bearer $tokenGestor")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest)
    }

    @Test
    @Order(4)
    @DisplayName("CT04: Valor Limite - Tarefa com Titulo longo deve falhar")
    fun `CT04 - Excecao Titulo Longo (Valor Limite)`() {
        // Gera uma string com 201 caracteres (limite é 200)
        val tituloLongo = "A".repeat(201) 

        val dto = CreateTaskDto(
            title = tituloLongo, // INVÁLIDO (> 200 caracteres)
            description = "Descricao válida para teste de limite",
            projectId = idProjetoAtivo,
            assignedToId = idFuncionario
        )

        mockMvc.perform(post("/api/tasks")
            .header("Authorization", "Bearer $tokenGestor")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest) // Espera erro 400
    }

    @Test
    @Order(5)
    @DisplayName("CT05: Valor Limite - Descricao curta deve falhar")
    fun `CT05 - Excecao Descricao Curta (Valor Limite)`() {
        val dto = CreateTaskDto(
            title = "Titulo Válido",
            description = "Curto", // INVÁLIDO (< 10 caracteres - RN02)
            projectId = idProjetoAtivo,
            assignedToId = idFuncionario
        )

        mockMvc.perform(post("/api/tasks")
            .header("Authorization", "Bearer $tokenGestor")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest)
    }

    @Test
    @Order(6)
    @DisplayName("CT06: Integridade - Criacao de Tarefa com Projeto Inexistente deve falhar")
    fun `CT06 - Excecao Projeto Inexistente (Integridade)`() {
        val dto = CreateTaskDto(
            title = "Tarefa Sem Projeto",
            description = "Descricao válida para teste",
            projectId = "uuid-inexistente-999", // INVÁLIDO
            assignedToId = idFuncionario
        )

        mockMvc.perform(post("/api/tasks")
            .header("Authorization", "Bearer $tokenGestor")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound) // 404 (Lancado pelo Service)
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Projeto")))
    }

    @Test
    @Order(7)
    @DisplayName("CT07: Role (Papel) - Funcionario sem permissao tenta criar tarefa deve falhar")
    fun `CT07 - Excecao Usuario sem Permissao (Role)`() {
        // Joao (FUNCIONARIO) tenta criar uma tarefa
        val dto = CreateTaskDto(
            title = "Tentativa do Joao",
            description = "Joao tentando criar tarefa sem ser gestor.",
            projectId = idProjetoAtivo,
            assignedToId = idFuncionario
        )

        mockMvc.perform(post("/api/tasks")
            .header("Authorization", "Bearer $tokenFuncionario") // TOKEN DO JOaO
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden) // 403 Forbidden
    }

    @Test
    @Order(8)
    @DisplayName("CT08: Fluxo Alternativo - Criar Tarefa com Prioridade URGENTE deve ter sucesso")
    fun `CT08 - Fluxo Alternativo Prioridade URGENTE`() {
        val dto = CreateTaskDto(
            title = "Vazamento Gás",
            description = "Vazamento critico na tubulacao principal.",
            projectId = idProjetoAtivo,
            assignedToId = idFuncionario,
            priority = TaskPriority.URGENTE  
        )

        mockMvc.perform(post("/api/tasks")
            .header("Authorization", "Bearer $tokenGestor")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.priority").value("URGENTE"))
    }

    // --- MÉTODOS AUXILIARES (HELPERS) ---

    private fun loginAndGetToken(user: String, pass: String): String {
        val loginReq = LoginRequestDto(user, pass)
        val result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk)
            .andReturn()
        
        val node = objectMapper.readTree(result.response.contentAsString)
        return node.get("accessToken").asText()
    }

    private fun createEmployee(tokenAuth: String, username: String, roleName: String): String {
        val dto = CreateEmployeeDto(
            username = username, password = "password123", fullName = "Nome $username",
            email = "$username@mail.com", registrationNumber = "REG-$username",
            role = UserRole.valueOf(roleName), department = Department.ENGENHARIA,
            status = EmployeeStatus.ACTIVE, phone = null, hireDate = LocalDate.now()
        )
        
        val result = mockMvc.perform(post("/api/employees")
            .header("Authorization", "Bearer $tokenAuth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated)
            .andReturn()
            
        return objectMapper.readTree(result.response.contentAsString).get("id").asText()
    }

    private fun createProject(tokenAuth: String, name: String, status: ProjectStatus): String {
        val dto = CreateProjectDto(
            name = name, description = "Desc", status = status,
            budget = BigDecimal("1000"), managerId = idGestor
        )
        
        val result = mockMvc.perform(post("/api/projects")
            .header("Authorization", "Bearer $tokenAuth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated)
            .andReturn()
            
        return objectMapper.readTree(result.response.contentAsString).get("id").asText()
    }
}