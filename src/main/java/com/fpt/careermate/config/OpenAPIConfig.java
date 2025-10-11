package com.fpt.careermate.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API-Pregnancy System ",
                version = "1.0",
                description = "REST API description...",

                contact = @Contact(name = "Your Name", email = "your.email@example.com")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Server")
        },

        security = {@SecurityRequirement(name = "bearerToken")}


)
@SecuritySchemes({
        @SecurityScheme(
                name = "bearerToken",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT"
        ),
//        @SecurityScheme(
//                name = "cookieAuth",
//                type = SecuritySchemeType.APIKEY,
//                in = SecuritySchemeIn.COOKIE,
//                paramName = "refreshToken"
//        )
})

public class OpenAPIConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("api-service-1")
                .packagesToScan("com.fpt.careermate.web.rest") // Thay bằng package của bạn
                .build();
    }
}
