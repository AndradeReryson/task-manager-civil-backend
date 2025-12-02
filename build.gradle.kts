import java.util.Properties
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.8"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
  id("com.adarshr.test-logger") version "4.0.0"
}

group = "com.taskmanager"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
	}
}

repositories {
	mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // OpenAPI/Swagger UI Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0") // Use a versão mais recente

    // Testes
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1") 
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17" // <--- MUDE DE "1.8" PARA "17"
    }
}

// 1. Define a localização do arquivo de credenciais
val localPropsFile = project.file("local.properties")

// 2. Se o arquivo existir (apenas em desenvolvimento local), leia suas propriedades
if (localPropsFile.exists()) {
    val localProperties = Properties()
    // Usa 'use' para garantir que o stream seja fechado
    localPropsFile.inputStream().use { 
        localProperties.load(it) 
    }

    // 3. Configura a tarefa 'bootRun' para usar essas propriedades
    tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
        localProperties.forEach { key, value ->
            // Injeta cada par chave-valor como uma System Property
            systemProperties[key.toString()] = value.toString()
        }
    }
}

// Configuração do Test Logger
testlogger {
    theme = com.adarshr.gradle.testlogger.theme.ThemeType.STANDARD
    showSummary = false
    showPassed = true
    showSkipped = true
    showFailed = true
    showStandardStreams = false
    showPassedStandardStreams = false
    showSkippedStandardStreams = false
    showFailedStandardStreams = true
}