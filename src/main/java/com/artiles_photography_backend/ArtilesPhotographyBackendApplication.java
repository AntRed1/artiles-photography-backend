package com.artiles_photography_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArtilesPhotographyBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(ArtilesPhotographyBackendApplication.class, args);
	}

}
