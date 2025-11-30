package com.taskmanager.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan // <--- NOVO IMPORT
import org.springframework.boot.context.properties.EnableConfigurationProperties
import com.taskmanager.demo.config.JwtProperties

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
// ATENÇÃO: Escaneia o pacote raiz (com.taskmanager) para incluir todas as subpastas (application, presentation, domain, etc.)
@ComponentScan(basePackages = ["com.taskmanager"]) 
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}