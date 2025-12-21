package com.fpt.careermate;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class CareermateApplication {

	private static final String TIMEZONE = "Asia/Ho_Chi_Minh";

	public static void main(String[] args) {
		// Set JVM default timezone BEFORE Spring context starts
		TimeZone.setDefault(TimeZone.getTimeZone(TIMEZONE));
		SpringApplication.run(CareermateApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// Ensure timezone is set after Spring context initialization
		TimeZone.setDefault(TimeZone.getTimeZone(TIMEZONE));
		System.out.println("=== Application Timezone set to: " + TIMEZONE + " ===");
		System.out.println("=== Current time: " + java.time.LocalDateTime.now() + " ===");
	}

}
