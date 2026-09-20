package dev.onyango.permission_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PermissionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PermissionServiceApplication.class, args);
	}

}
