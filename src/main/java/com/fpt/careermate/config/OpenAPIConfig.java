package com.fpt.careermate.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API-CareerMate System ",
                version = "1.0",
                description = "REST API description...",

                contact = @Contact(name = "Your Name", email = "your.email@example.com")
        ),
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

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.swagger.server.url:}")
    private String customServerUrl;

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("api-service-1")
                .packagesToScan("com.fpt.careermate.services") // Scan all services packages
                .build();
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            // Clear any existing servers
            openApi.getServers().clear();

            // If custom server URL is provided, use it (for production)
            if (customServerUrl != null && !customServerUrl.isEmpty()) {
                Server prodServer = new Server();
                prodServer.setUrl(customServerUrl);
                prodServer.setDescription("Production Server");
                openApi.addServersItem(prodServer);
            }

            // Always add localhost as fallback
            Server localServer = new Server();
            localServer.setUrl("http://localhost:" + serverPort);
            localServer.setDescription("Local Development");
            openApi.addServersItem(localServer);
        };
    }
}
