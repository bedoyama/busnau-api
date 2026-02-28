package com.bedoyarama.busnau;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BusnauApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusnauApiApplication.class, args);
	}

	@GetMapping("/")
	public String home() {
		return "Welcome to the Busnau API!";
	}
}
