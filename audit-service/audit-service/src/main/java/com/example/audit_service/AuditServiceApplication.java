package com.example.audit_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableRabbit
public class AuditServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuditServiceApplication.class, args);
	}
}
