package com.taskmanager.demo.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val secret: String,
    val accessExpirationMs: Long,
    val refreshExpirationMs: Long
)