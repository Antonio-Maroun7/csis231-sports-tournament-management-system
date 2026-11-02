package com.antonio.sportstournament;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Sports Tournament Management System REST API (Spring Boot).
 * {@code @SpringBootApplication} turns on component scanning for all sub-packages
 * (user, auth, team, player, tournament, registration, security, ...) and auto-configuration.
 */
@SpringBootApplication
public class SportstournamentApplication {

	/**
	 * Starts the embedded web server and the Spring application context.
	 *
	 * @param args command-line arguments, passed unchanged to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(SportstournamentApplication.class, args);
	}

}
