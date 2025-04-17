package com.seva.propert.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.seva.propert.converter.TaskInConverter;

@Configuration
public class WebConfig implements WebMvcConfigurer{

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new TaskInConverter());
    }
    
    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/**")
    //             .allowedOrigins("http://localhost:4200")
    //             .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    //             .allowedHeaders("*")
    //             .allowCredentials(true);
    // }

    @Bean
	public static PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    // @Bean
    // public UserDetailsService userDetailsService(){
    //     UserDetails adminUser = User.builder()
    //     		.username("admin")
    //     		.password(passwordEncoder().encode("admin"))
    //     		.roles("ADMIN")
    //     		.authorities("CREATE", "READ", "UPDATE", "DELETE")
    //     		.build();
        
    //     UserDetails normalUser = User.builder()
    //     		.username("user")
    //     		.password(passwordEncoder().encode("1234"))
    //     		.roles("USER")
    //     		.authorities("READ")
    //     		.build();
        

    //     return new InMemoryUserDetailsManager(adminUser, normalUser);
    // }

    // @Bean
    // public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
    //     return builder.getAuthenticationManager();
    // }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
