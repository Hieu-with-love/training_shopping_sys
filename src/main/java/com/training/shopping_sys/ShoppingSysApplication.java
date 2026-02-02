package com.training.shopping_sys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Shopping System Application - Main Entry Point.
 * 
 * <p>Spring Boot application for an e-commerce shopping system.
 * This application provides a complete web-based platform for product
 * management, order processing, and user authentication.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Product catalog with search and filtering</li>
 *   <li>Shopping cart and order management</li>
 *   <li>User authentication with Spring Security</li>
 *   <li>Image handling for product photos</li>
 *   <li>RESTful APIs with Swagger documentation</li>
 *   <li>Thymeleaf-based web interface</li>
 * </ul>
 * 
 * <p>Technologies used:</p>
 * <ul>
 *   <li>Spring Boot 3.x</li>
 *   <li>Spring Security</li>
 *   <li>Spring Data JPA</li>
 *   <li>PostgreSQL Database</li>
 *   <li>Thymeleaf Template Engine</li>
 *   <li>Swagger/OpenAPI 3.0</li>
 * </ul>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class ShoppingSysApplication {

	/**
	 * Application entry point.
	 * 
	 * <p>Starts the Spring Boot application and initializes all
	 * components, configurations, and services.</p>
	 * 
	 * @param args Command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(ShoppingSysApplication.class, args);
	}

}
