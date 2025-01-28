package com.seva.propert.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPI30Configuration {
	@Value("${propert.openapi.dev-url}")
	private String devUrl;

	@Value("${propert.openapi.prod-url}")
	private String prodUrl;

	private SecurityScheme createAPIKeyScheme() {
		return new SecurityScheme()
				.name("Bearer Authentication")
				.type(SecurityScheme.Type.HTTP)
				.bearerFormat("JWT")
				.scheme("bearer");
	}

    @Bean
    public OpenAPI yogaScheduleOpenAPI() {
		
		Server devServer = new Server();
		devServer.setUrl(devUrl);
		devServer.setDescription("Server URL in Development environment");

		Server prodServer = new Server();
		prodServer.setUrl(prodUrl);
		prodServer.setDescription("Server URL in Production environment");
		
        return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
				.components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
                .info(new Info().title("Propert API")
                .description("API to manage projects/tasks and workflows.")
                .version("v3.1.0")
                .license(new License().name("License of API").url("API license URL")))
                .externalDocs(new ExternalDocumentation()
                					.description("SpringShop Wiki Documentation")
                					.url("https://springshop.wiki.github.org/docs"));
                //.servers(List.of(devServer,prodServer));
    }
}