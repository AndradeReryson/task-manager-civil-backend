# 🏗️ TaskManager Civil Backend

## 📋 Descrição do Projeto

O **TaskManager Civil** é um sistema de gerenciamento de tarefas e projetos focado no setor de engenharia civil e construção. Este repositório contém a aplicação *backend*, responsável por toda a lógica de negócio, persistência de dados e autenticação via JWT.

A arquitetura do backend segue o padrão DDD (Domain-Driven Design) em camadas, garantindo alta manutenibilidade, escalabilidade e separação clara de responsabilidades.

## ✨ Funcionalidades Chave da API

A API é totalmente **RESTful** e implementa um sistema de gerenciamento completo:

  * **Segurança Robusta (JWT):** Autenticação *Stateless* com tokens de acesso e refresh token.
  * **Controle de Acesso por Role:** Utiliza roles (`ADMIN`, `GESTOR_OBRAS`, `FUNCIONARIO`) para permissão granular em nível de endpoint (`@PreAuthorize`).
  * **CRUD Completo:** Gerenciamento de Usuários, Colaboradores, Projetos, Equipes, Tarefas, Documentos e Registros Financeiros.
  * **Filtros Avançados:** Implementação de consultas complexas e dinâmicas através do padrão **JPA Specification** em todas as listagens (`findAll`).
  * **Soft Delete:** Exclusão lógica em todas as entidades, permitindo a visualização de itens deletados (Lixeira) via filtro `?isActive=false`.
  * **Inicialização de Dados:** Criação automática do usuário `admin` essencial via script `data.sql` na inicialização do servidor (ambiente de desenvolvimento).

## 💻 Tecnologias Utilizadas

| Categoria | Tecnologia | Versão/Padrão |
| :--- | :--- | :--- |
| **Linguagem** | Kotlin | 1.9.25 |
| **Framework** | Spring Boot | 3.x |
| **Banco de Dados** | PostgreSQL | (Driver) |
| **Persistência** | Spring Data JPA / Hibernate | 6.x |
| **Segurança** | Spring Security | 6.x |
| **Autenticação** | JWT (JSON Web Tokens) | JJWT 0.12.3 |
| **Serialização** | Jackson | (Suporte Kotlin) |
| **Documentação** | SpringDoc OpenAPI | 2.3.0 |

## 🚀 Como Executar o Projeto Localmente

Siga os passos abaixo para configurar e iniciar o backend na sua máquina.

### 1\. Requisitos

  * **Java Development Kit (JDK):** Versão 17 ou superior (recomendado 21).
  * **PostgreSQL:** Servidor de banco de dados rodando.
  * **Gradle:** Para construção do projeto.

### 2\. Configuração do Banco de Dados

Crie um banco de dados PostgreSQL vazio (ex: `taskmanager_civil_db`).

Na raiz do projeto (`backend/`), crie um arquivo chamado **`local.properties`** para configurar as credenciais do seu banco de dados local (este arquivo é lido pelo `build.gradle.kts` na execução `bootRun`):

```properties
# local.properties (Não deve ser commitado no Git)

# Configuração do PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager_civil_db
spring.datasource.username=seu_usuario_postgres
spring.datasource.password=sua_senha_postgres
```

### 3\. Executar a Aplicação (Primeira Inicialização)

O projeto está configurado com `ddl-auto=create-drop` (temporariamente, para fins de setup) e `spring.sql.init.mode=always`.

1.  **Limpar e Compilar:**
    ```bash
    ./gradlew clean build
    ```
2.  **Executar:**
    O servidor iniciará, e o Hibernate criará o esquema (`users`, `employees`, etc.) e o `data.sql` **inserirá o usuário `admin` automaticamente**.
    ```bash
    ./gradlew bootRun
    ```

O servidor estará ativo em `http://localhost:8080`.

-----

## 5\. 📖 Como Acessar a Documentação da API (Swagger)

Após a inicialização bem-sucedida, você pode acessar a documentação interativa da API no seu navegador:

1.  **URL do Swagger UI:**
    ```
    http://localhost:8080/swagger-ui.html
    ```
2.  **Autenticação:**
      * No Swagger UI, utilize o endpoint **`POST /api/auth/login`**.
      * **Username:** `admin`
      * **Password:** `password`
      * Copie o `accessToken` retornado.
      * Clique no botão **"Authorize"** no topo da página e cole o token para testar os endpoints protegidos.

-----

## 🤝 Integração com o Front-end KMP
Esta API foi desenhada para ser consumida pelo projeto Kotlin Multiplatform. O colega do front-end deve:

Utilizar a interface Swagger UI para obter a estrutura exata de todos os DTOs (ProjectRequestDTO, TaskResponseDTO, etc.).

Usar bibliotecas como Ktor Client ou Kotlinx Serialization para lidar com as requisições HTTP e o parseamento de JSON.

---

## 🤝 Guia de Conexão Frontend (KMP/WASM)

Esta seção resume os pontos críticos para o time de frontend configurar o **Ktor Client** e mapear os DTOs corretamente.

### 1. Configuração do Cliente HTTP (Ktor)

* **URL Base:** O backend está ativo em `http://localhost:8080/api` (ambiente de desenvolvimento). No arquivo `ApiClient.kt` do frontend, a `BASE_URL` deve ser configurada para incluir o `/api/` e o domínio correto.
* **CORS:** O backend está configurado para aceitar requisições de desenvolvimento (`http://localhost:8080`) e produção (`https://seu-dominio.github.io`), permitindo todos os métodos HTTP (GET, POST, etc.).

### 2. Fluxo de Autenticação (JWT)

O sistema utiliza tokens JWT e Refresh Tokens.

* **Login:** O Access Token é obtido via `POST /api/auth/login`.
* **Duração:** O Access Token expira em **1 hora** (3600 segundos).
* **Renovação:** Se o token vencer, use o `refreshToken` no endpoint `POST /api/auth/refresh` para obter um novo Access Token sem relogar.
* **Mecanismo Ktor:** O Ktor Client deve ser configurado com o *plugin* **Auth Bearer**. O *frontend* é responsável por armazenar o token; o `ApiClient` o anexa automaticamente ao header `Authorization`.

### 3. Mapeamento de Contratos (DTOs)

Os modelos de dados (DTOs) no frontend (Kotlin) **devem** corresponder exatamente às estruturas JSON definidas em `JSON_EXAMPLES.md`.

* **Datas e Horas:** Campos de data e hora (Ex: `createdAt`, `dueDate`) estão no formato ISO 8601 (`2024-01-20T10:30:00Z`) e devem ser mapeados utilizando o **`kotlinx.datetime`**.
* **Enums:** Todos os campos de status e tipo (Ex: `status`, `department`, `type`) são *strings* em **UPPERCASE** (Ex: `EM_ANDAMENTO`, `DESPESA`).
* **Tratamento de Erros:** O formato de erro padrão é consistente, retornando um objeto JSON com campos como `timestamp`, `status`, `message` e a lista detalhada de `errors` (para validação).

### 4. Endpoints de Referência e Permissões

A segurança é gerenciada pelo backend via Roles.

| Recurso | Tipo | Endpoint de Referência | Role Mínima | Filtro Exemplo (Specifications) |
| :--- | :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/auth/login` | Público | N/A |
| **Colaborador** | `GET` | `/api/employees` | FUNCIONARIO | `?department=ENGENHARIA&isActive=false` |
| **Projeto** | `POST` | `/api/projects` | GESTOR_OBRAS | N/A (Criação) |
| **Tarefa** | `GET` | `/api/tasks` | FUNCIONARIO | `?projectId={id}&status=PENDENTE` |
| **Financeiro** | `GET` | `/api/financial` | GESTOR_OBRAS | `?type=RECEITA&category=VENDA` |
| **Soft Delete** | `GET` | `/api/{recurso}` | Variável | `?isActive=false` (Para ver a Lixeira) |

-----  

## 👥 Contribuidores
Projeto Interdisciplinar - 6º Semestre

Integrantes:

- 1º: Breno Ribeiro Souza
- 2º: Daniele Capistrano
- 3º: Diego Bicelli 
- 4º: Lucas Trindade
- 5º: Gustavo dos Anjos
- 6º: Reryson Santos de Andrade
- 7º: Ulisses Antonelli
