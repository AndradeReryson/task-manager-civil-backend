## **🔑 SETUP: Populando banco de dados**

## Inserção e 🔎 Leitura (CREATE & READ)

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


## **⚠️ Pré-requisitos para a gravação:**

1.  Tenha os IDs salvos no bloco de notas:
      * `ID_PROJETO_ALPHA` (Projeto Ativo)
      * `ID_JOAO_EMP` (Funcionário João)
      * `ID_MARIA_EMP` (Gestora Maria - será a responsável em alguns casos)
2.  Tenha dois Tokens JWT à mão:
      * **Token da MARIA** (Gestora - para a maioria dos testes)
      * **Token do JOÃO** (Funcionário - **apenas para o CT07**)




-----

### 🟢 CT01: Fluxo Principal (Sucesso Completo)

  * **Objetivo:** Criar uma tarefa padrão com sucesso.
  * **Token:** MARIA
  * **Método:** `POST`
  * **URL:** `http://localhost:8080/api/tasks`
  * **JSON:**

<!-- end list -->

```json
{
  "title": "Fundação Torre A",
  "description": "Escavação e concretagem da base conforme planta.",
  "projectId": "COLE_O_ID_DO_PROJETO_ALPHA_AQUI",
  "assignedToId": "COLE_O_ID_DO_JOAO_AQUI",
  "priority": "ALTA",
  "dueDate": "2025-05-20T10:00:00",
  "status": "PENDENTE"
}
```

  * **Esperado:** `201 Created`

-----

### 🔴 CT02: Validação - Título Vazio

  * **Objetivo:** Tentar criar tarefa com título vazio.
  * **Token:** MARIA
  * **Método:** `POST`
  * **URL:** `http://localhost:8080/api/tasks`
  * **JSON:**

<!-- end list -->

```json
{
  "title": "",
  "description": "Descrição válida para teste.",
  "projectId": "COLE_O_ID_DO_PROJETO_ALPHA_AQUI",
  "assignedToId": "COLE_O_ID_DO_JOAO_AQUI"
}
```

  * **Esperado:** `400 Bad Request` (Erro de validação: título obrigatório)

-----

### 🔴 CT03: Validação - Título Curto (\< 3 caracteres)

  * **Objetivo:** Tentar criar tarefa com título muito curto.
  * **Token:** MARIA
  * **Método:** `POST`
  * **URL:** `http://localhost:8080/api/tasks`
  * **JSON:**

<!-- end list -->

```json
{
  "title": "Oi",
  "description": "Descrição válida para teste.",
  "projectId": "COLE_O_ID_DO_PROJETO_ALPHA_AQUI",
  "assignedToId": "COLE_O_ID_DO_JOAO_AQUI"
}
```

  * **Esperado:** `400 Bad Request` (Erro de validação: tamanho mínimo 3)

-----

### 🔴 CT04: Validação - Título Longo (\> 200 caracteres)

  * **Objetivo:** Tentar criar tarefa com título gigante.
  * **Token:** MARIA
  * **Método:** `POST`
  * **URL:** `http://localhost:8080/api/tasks`
  * **JSON:** (Copie este texto gigante abaixo no título)

<!-- end list -->

```json
{
  "title": "Este é um título extremamente longo criado propositalmente para testar o limite máximo de caracteres permitido pelo sistema que é de duzentos caracteres e nós precisamos garantir que o backend rejeite qualquer tentativa de salvar algo maior que isso para manter a integridade do banco de dados e da interface X",
  "description": "Descrição válida para teste.",
  "projectId": "COLE_O_ID_DO_PROJETO_ALPHA_AQUI",
  "assignedToId": "COLE_O_ID_DO_JOAO_AQUI"
}
```

  * **Esperado:** `400 Bad Request` (Erro de validação: tamanho máximo 200)

-----

### 🔴 CT05: Validação - Descrição Curta (\< 10 caracteres)

  * **Objetivo:** Tentar criar tarefa com descrição insuficiente.
  * **Token:** MARIA
  * **Método:** `POST`
  * **URL:** `http://localhost:8080/api/tasks`
  * **JSON:**

<!-- end list -->

```json
{
  "title": "Título Válido",
  "description": "Curto",
  "projectId": "COLE_O_ID_DO_PROJETO_ALPHA_AQUI",
  "assignedToId": "COLE_O_ID_DO_JOAO_AQUI"
}
```

  * **Esperado:** `400 Bad Request` (Erro de validação: descrição mínima 10)

-----

### 🔴 CT06: Integridade - Projeto Inexistente

  * **Objetivo:** Tentar vincular a um ID de projeto que não existe.
  * **Token:** MARIA
  * **Método:** `POST`
  * **URL:** `http://localhost:8080/api/tasks`
  * **JSON:**

<!-- end list -->

```json
{
  "title": "Tarefa Órfã",
  "description": "Tentativa de criar tarefa em projeto falso.",
  "projectId": "00000000-0000-0000-0000-000000000999", 
  "assignedToId": "COLE_O_ID_DO_JOAO_AQUI"
}
```

  * **Esperado:** `404 Not Found` (Mensagem: "Projeto não encontrado")

-----

### 🔒 CT07: Segurança - Usuário sem Permissão

  * **Objetivo:** Tentar criar tarefa usando um funcionário comum (João).
  * **Token:** **JOÃO** (⚠️ Troque o token no Header Authorization\!)
  * **Método:** `POST`
  * **URL:** `http://localhost:8080/api/tasks`
  * **JSON:**

<!-- end list -->

```json
{
  "title": "Tentativa do João",
  "description": "João tentando criar tarefa sem ser gestor.",
  "projectId": "COLE_O_ID_DO_PROJETO_ALPHA_AQUI",
  "assignedToId": "COLE_O_ID_DO_JOAO_AQUI"
}
```

  * **Esperado:** `403 Forbidden` (Acesso negado)

-----

### 🟢 CT08: Fluxo Alternativo - Prioridade Crítica

  * **Objetivo:** Criar tarefa com prioridade máxima.
  * **Token:** MARIA (⚠️ Volte para o token da Maria)
  * **Método:** `POST`
  * **URL:** `http://localhost:8080/api/tasks`
  * **JSON:**

<!-- end list -->

```json
{
  "title": "Vazamento de Gás",
  "description": "Vazamento crítico na tubulação principal. Risco alto.",
  "projectId": "COLE_O_ID_DO_PROJETO_ALPHA_AQUI",
  "assignedToId": "COLE_O_ID_DO_JOAO_AQUI",
  "priority": "URGENTE",
  "dueDate": "2025-03-02T08:00:00"
}
```

  * **Esperado:** `201 Created` (Verifique na resposta se `"priority": "CRITICA"`)