package com.fpt.careermate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Integration test - requires database connection. Run with: ./mvnw test -Dspring.profiles.active=test")
class CareermateApplicationTests {

	@Test
	void contextLoads() {
	}

}
