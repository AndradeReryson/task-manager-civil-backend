package com.taskmanager.infrastructure.filter

data class SearchCriteria(
    val key: String,        // Nome do campo (ex: "department", "status", "user.fullName")
    val operation: String,  // Operação (ex: ":", ">", "<", "like")
    val value: Any?
)