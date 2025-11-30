// MUDANÇA CRUCIAL: O pacote deve ser sub-pacote da sua Main Class (com.taskmanager.demo)
package com.taskmanager.demo.config 

import com.taskmanager.demo.security.CustomUserDetailsService // <-- CORRIGIDO
import com.taskmanager.demo.security.JwtAuthenticationFilter   // <-- CORRIGIDO

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
class SecurityConfig(
    // Certifique-se de que esses serviços estão sendo importados corretamente
    private val customUserDetailsService: CustomUserDetailsService,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /* 

    FUNÇÃO PARA OBTER UM HASH DA SENHA DO ADMIN

    @Bean
    fun generateNewAdminHash(passwordEncoder: PasswordEncoder): Boolean {
        val rawPassword = "password"
        
        // GERA O NOVO HASH USANDO O SEU PRÓPRIO ENCODER
        val newEncodedHash = passwordEncoder.encode(rawPassword)
        
        println("\n\n######################################################")
        println("### SUCESSO: NOVO HASH GERADO PARA ADMIN ###")
        println("###                                          ###")
        println("### NOVO HASH: $newEncodedHash") // O hash correto será impresso aqui
        println("###                                          ###")
        println("######################################################\n")
        
        // Retornamos true para que o Spring continue a inicialização
        return true 
    }
    */

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager = 
        authConfig.authenticationManager

    @Bean
    fun authenticationProvider(passwordEncoder: PasswordEncoder): DaoAuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(customUserDetailsService)
        provider.setPasswordEncoder(passwordEncoder)
        return provider
    }
    
    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.allowedOrigins = listOf("http://localhost:8080", "http://localhost:3000", "https://seu-app.github.io")
        config.allowedHeaders = listOf("*")
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        source.registerCorsConfiguration("/**", config)
        return source
    }

    /**
     * CONFIGURAÇÃO DE SEGURANÇA
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 1. Desabilita tudo que causa redirects de login
            .csrf { it.disable() }
            .formLogin { it.disable() } // ISSO VAI FUNCIONAR AGORA
            .httpBasic { it.disable() }
            .logout { it.disable() }

            // 2. Stateless
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            
            // 3. CORS e Erros
            .cors { it.configurationSource(corsConfigurationSource()) } 
            .exceptionHandling { 
                // Retorna 401 Unauthorized (JSON) ao invés de redirecionar
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) 
            }

            // 4. Autorização
            .authorizeHttpRequests { auth ->
                auth
                    // Swagger e Docs (REDUNDÂNCIA DE SEGURANÇA, mas útil)
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    
                    // Auth e Actuator
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll() 
                    
                    // Resto protegido
                    .anyRequest().authenticated()
            }
            
            // 5. Filtro JWT
            .authenticationProvider(authenticationProvider(passwordEncoder())) 
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    /**
     * IGNORA TOTALMENTE O SWAGGER (Bypassing Spring Security)
     */
    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/webjars/**",
                "/favicon.ico"
            )
        }
    }
}

/**
 * CONFIGURAÇÃO OPENAPI
 */
@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "BearerAuth"
        return OpenAPI()
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components().addSecuritySchemes(
                    securitySchemeName,
                    SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
    }
}