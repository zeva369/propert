package com.sidus.propert.context.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.sidus.propert.service.JWTService;
import com.sidus.propert.service.TokenStoreService;
import com.sidus.propert.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true,
					  prePostEnabled = true)
@EnableAutoConfiguration(exclude = { ErrorMvcAutoConfiguration.class })
@RequiredArgsConstructor
public class CustomWebSecurityConfigurerAdapter {

    private final JWTService jwtService;

	private static final String[] AUTH_WHITELIST = {
            "/auth/login/**",
			"/auth/refresh/**",
			"/users/**",
            "/guest/**",
    };
    
    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter(jwtService, authenticationEntryPoint(), AUTH_WHITELIST);
    }
	
    @Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable())                  
        			.cors( cors -> cors.configurationSource(corsConfigurationSource()))
					.authorizeHttpRequests(requests -> requests.requestMatchers(AUTH_WHITELIST)
							    							   .permitAll()

							    							   .requestMatchers("/authenticated/**",
																	   "/auth/logout")
															   .hasRole("USER")
															   .anyRequest()
															   .authenticated())
                    .addFilterBefore(jwtAuthenticationFilter(),UsernamePasswordAuthenticationFilter.class)       
					.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint())
					                         .accessDeniedHandler(accessDeniedHandler()));
		
		return httpSecurity.build();
	}

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
    	    String responseString = "{\"status\":\"401\"}";
		    response.setStatus(HttpStatus.UNAUTHORIZED.value());
		    response.getOutputStream().write(responseString.getBytes(StandardCharsets.UTF_8));
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
    	    String responseString = "{\"status\":\"403\"}";
		    response.setStatus(HttpStatus.FORBIDDEN.value());
		    response.getOutputStream().write(responseString.getBytes(StandardCharsets.UTF_8));
        };
    }

	// @Bean
    // public AccessDeniedHandler accessDeniedHandler() {
    //     return new CustomAccessDeniedHandler();
    // }
	
	// @Bean
    // public AuthenticationEntryPoint authenticationEntryPoint() {
    //     return new DelegatedAuthenticationEntryPoint();
    // }
	
    @Bean
	@Primary
	public CorsConfigurationSource corsConfigurationSource() {
		//CorsConfiguration corsConfig = new CorsConfiguration().applyPermitDefaultValues();
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://127.0.0.1:4200"));
		corsConfig.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "PATCH","DELETE", "OPTIONS"));
		corsConfig.setAllowCredentials(true);
		corsConfig.addAllowedHeader("*");//Arrays.asList("Authorization", "Content-Type"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
		
		return source;
	}

	@Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
		CorsFilter filter = new CorsFilter(corsConfigurationSource());
		//The magic is here: The custom processor override the rejectRequest and implements the
		//logic for returning a JSON response more accurated for a REST api
		filter.setCorsProcessor(new CustomCorsProcessor());
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(filter);
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}

	
}
