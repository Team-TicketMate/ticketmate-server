package com.ticketmate.backend.util.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@OpenAPIDefinition(
        info = @Info(
                title = "🎫 티켓메이트 : TICKET-MATE 🎫",
                description = """
                        ### 🌐 티켓메이트 웹사이트 🌐 : ticketmate.site
                        [**웹사이트 바로가기**](https://ticketmate.site)

                        ### 💻 **GitHub 저장소**
                        - **[백엔드 소스코드](https://github.com/Team-TicketMate/ticketmate-server)**
                          백엔드 개발에 관심이 있다면 저장소를 방문해보세요.
                        """,
                version = "1.0v"
        ),
        servers = {
                @Server(url = "https://api.ticketmate.site", description = "메인 서버"),
                @Server(url = "https://test.ticketmate.site", description = "테스트 서버"),
                @Server(url = "http://localhost:8080", description = "로컬 서버")
        }
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme apiKey = new SecurityScheme()
                .type(Type.HTTP)
                .in(In.HEADER)
                .name("Authorization")
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Token");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("Bearer Token", apiKey))
                .addSecurityItem(securityRequirement)
                .servers(List.of(
                                new io.swagger.v3.oas.models.servers.Server()
                                        .url("http://localhost:8080")
                                        .description("로컬 서버"),
                                new io.swagger.v3.oas.models.servers.Server()
                                        .url("https://test.ticketmate.site")
                                        .description("테스트 서버"),
                                new io.swagger.v3.oas.models.servers.Server()
                                        .url("https://api.ticketmate.site")
                                        .description("메인 서버")
                        )
                );
    }
}
