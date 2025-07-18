package com.sidus.propert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class PropertBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PropertBackendApplication.class, args);
	}

}
