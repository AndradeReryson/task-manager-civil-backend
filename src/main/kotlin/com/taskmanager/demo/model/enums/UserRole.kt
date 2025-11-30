package com.taskmanager.demo.model.enums

// Define as roles de autorização da aplicação, conforme a Matriz de Permissões.
enum class UserRole {
    ADMIN,
    GESTOR_OBRAS,
    LIDER_EQUIPE,
    FUNCIONARIO;

    // Método auxiliar para formatar a role para o padrão do Spring Security (ex: ROLE_ADMIN)
    fun withPrefix(): String = "ROLE_$name"
}