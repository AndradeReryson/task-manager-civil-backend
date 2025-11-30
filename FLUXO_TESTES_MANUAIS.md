Entendido\! Vou consolidar o plano de testes completo (Inserção, Leitura, Atualização e Exclusão) em um único arquivo Markdown, seguindo o formato de ciclo de vida e a estrutura de módulos que definimos.

-----

# 📋 Plano de Testes Funcionais da API REST (CRUD Completo)

**Projeto:** TaskManager Civil Backend
**Objetivo:** Validar o ciclo de vida completo dos recursos, filtros avançados e regras de segurança (Soft Delete e Roles).
**Base URL:** `http://localhost:8080/api`

-----

## 🔑 SETUP: Login e Tokens Essenciais

A ordem de login é crucial: `ADMIN` para criar a base, `MARIA` (Gestora) para criar o conteúdo de negócio.

### 1\. Login do ADMIN (Para criar Maria e João)

  * **Método:** `POST /api/auth/login`
  * **Payload:**
    ```json
    {
      "username": "admin",
      "password": "password"
    }
    ```
  * **AÇÃO PÓS-REQUISIÇÃO:** Copie o `accessToken` do **admin** e defina-o no cabeçalho `Authorization: Bearer <token>`.

### 2\. Login da GESTORA (Para Criar Projetos/Tarefas)

  * **Método:** `POST /api/auth/login`
  * **Corpo:**
    ```json
    {
      "username": "maria.santos",
      "password": "senha456"
    }
    ```
  * **AÇÃO PÓS-REQUISIÇÃO:** Após a criação dos usuários na FASE 1, use o token da **Maria** para todas as requisições de criação de Projeto/Tarefa/Financeiro.

-----

## FASE 1: ➕ Inserção e 🔎 Leitura (CREATE & READ)

Esta fase cria os dados essenciais e testa a funcionalidade básica de listagem e filtragem (`findAll`).

### 1\. Colaboradores (Employees)

| Endpoint | Tipo | Cenário | JSON Exemplo (Criação) | Ação Pós-Req |
| :--- | :--- | :--- | :--- | :--- |
| `/api/employees` | `POST` | Criar GESTOR (Maria) | `{"username": "maria.santos", "password": "senha456", "fullName": "Maria Santos", "registrationNumber": "GTO-2025", "role": "GESTOR_OBRAS", "department": "ENGENHARIA", "status": "ACTIVE", "hireDate": "2025-01-20"}` | **Salvar ID:** `ID_MARIA_EMP` |
| `/api/employees` | `POST` | Criar FUNCIONARIO (João) | `{"username": "joao.silva", "password": "senha789", "fullName": "João Silva", "registrationNumber": "FUN-2025", "role": "FUNCIONARIO", "department": "OPERACIONAL", "status": "ACTIVE", "hireDate": "2025-01-20"}` | **Salvar ID:** `ID_JOAO_EMP` |
| `/api/employees` | `GET` | **Leitura com Filtro** | N/A | `GET /api/employees?department=ENGENHARIA&search=Maria` (Esperado: Maria) |
| `/api/employees/{id}` | `GET` | Leitura por ID | N/A | `GET /api/employees/<ID_MARIA_EMP>` |

### 2\. Projetos (Projects)

*Use o Token da Maria.*

| Endpoint | Tipo | Cenário | JSON Exemplo (Criação) | Ação Pós-Req |
| :--- | :--- | :--- | :--- | :--- |
| `/api/projects` | `POST` | Criar Projeto Alpha | `{"name": "Projeto Alpha - Horizonte", "status": "EM_ANDAMENTO", "managerId": "<ID_MARIA_EMP>", "budget": 2000000.00}` | **Salvar ID:** `ID_PROJETO_ALPHA` |
| `/api/projects` | `POST` | Criar Projeto Beta | `{"name": "Projeto Beta - Concluído", "status": "CONCLUIDO", "managerId": "<ID_MARIA_EMP>", "budget": 500000.00}` | N/A |
| `/api/projects` | `GET` | **Leitura com Filtro** | N/A | `GET /api/projects?status=EM_ANDAMENTO&search=Alpha` |

### 3\. Tarefas (Tasks)

*Use o Token da Maria.*

| Endpoint | Tipo | Cenário | JSON Exemplo (Criação) | Ação Pós-Req |
| :--- | :--- | :--- | :--- | :--- |
| `/api/tasks` | `POST` | Tarefa PENDENTE (para João) | `{"title": "Ajuste Planta", "projectId": "<ID_PROJETO_ALPHA>", "assignedToId": "<ID_JOAO_EMP>", "status": "PENDENTE", "priority": "ALTA", "dueDate": "2025-04-10T10:00:00"}` | **Salvar ID:** `ID_TAREFA_A` |
| `/api/tasks` | `POST` | Tarefa CONCLUIDA (para Maria) | `{"title": "Revisão Orçamento", "projectId": "<ID_PROJETO_ALPHA>", "assignedToId": "<ID_MARIA_EMP>", "status": "CONCLUIDA", "priority": "BAIXA"}` | N/A |
| `/api/tasks` | `GET` | **Filtro por Atribuição** | N/A | `GET /api/tasks?assignedToId=<ID_JOAO_EMP>&status=PENDENTE` |

### 4\. Financeiro (Financial)

*Use o Token da Maria.*

| Endpoint | Tipo | Cenário | JSON Exemplo (Criação) | Ação Pós-Req |
| :--- | :--- | :--- | :--- | :--- |
| `/api/financial` | `POST` | Criar Despesa | `{"description": "Compra CIMENTO", "type": "DESPESA", "category": "MATERIAL", "amount": 50000.00, "transactionDate": "2025-03-05", "projectId": "<ID_PROJETO_ALPHA>"}` | **Salvar ID:** `ID_FIN_D` |
| `/api/financial` | `POST` | Criar Receita | `{"description": "Pagamento Parcela", "type": "RECEITA", "category": "VENDA", "amount": 250000.00, "transactionDate": "2025-03-10", "projectId": "<ID_PROJETO_ALPHA>"}` | N/A |
| `/api/financial` | `GET` | **Filtro por Tipo** | N/A | `GET /api/financial?type=RECEITA&category=VENDA` |

-----

-----

## FASE 2: ✏️ Atualização e 🗑️ Exclusão (UPDATE & DELETE)

Esta fase testa a mutabilidade dos dados (`PUT`) e a exclusão lógica (*Soft Delete* via `DELETE`).

### 1\. Projetos (Projects)

| Endpoint | Tipo | Cenário | JSON Exemplo | Permissão |
| :--- | :--- | :--- | :--- | :--- |
| `/api/projects/<ID_PROJETO_ALPHA>` | `PUT` | Mudar Status/Descrição | `{"status": "PAUSADO", "description": "Obra pausada para análise de custos"}` | `GESTOR_OBRAS` |
| `/api/projects/<ID_PROJETO_ALPHA>` | `DELETE` | Excluir Logicamente | N/A | `GESTOR_OBRAS` ou `ADMIN` |
| `/api/projects` | `GET` | **Verificar Soft Delete** | N/A | `GET /api/projects` (Não deve mais aparecer o Projeto Alpha, assumindo filtro de ativos no findAll). |

### 2\. Tarefas (Tasks)

| Endpoint | Tipo | Cenário | JSON Exemplo | Permissão |
| :--- | :--- | :--- | :--- | :--- |
| `/api/tasks/<ID_TAREFA_A>` | `PUT` | Mudar Prioridade | `{"priority": "URGENTE"}` | `GESTOR_OBRAS` |
| `/api/tasks/<ID_TAREFA_A>` | `DELETE` | Excluir Logicamente | N/A | `GESTOR_OBRAS` ou `ADMIN` |

### 3\. Colaboradores (Employees)

*Requisite o Token do ADMIN para esta fase, pois a exclusão de Employee é restrita.*

| Endpoint | Tipo | Cenário | JSON Exemplo | Permissão |
| :--- | :--- | :--- | :--- | :--- |
| `/api/employees/<ID_JOAO_EMP>` | `PUT` | Atualizar Função | `{"department": "ENGENHARIA", "role": "LIDER_EQUIPE"}` | `GESTOR_OBRAS` |
| `/api/employees/<ID_JOAO_EMP>` | `DELETE` | Desligar (*Soft Delete*) | N/A | `ADMIN` |

-----

## 📊 FASE 3: Dashboard (Verificação Final)

| Endpoint | Tipo | Cenário | Teste | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| `/api/dashboard` | `GET` | Métricas Agregadas | N/A | `totalProjects` deve ser 2. `tasksInProgress` deve ser 0 (pois ambas as tarefas foram pausadas ou concluídas/deletadas). `netBalance` deve refletir a Receita (250k) menos a Despesa (50k). |