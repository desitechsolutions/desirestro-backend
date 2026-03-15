package com.dts.restro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.properties",
        properties = "spring.profiles.active=test")
class DesiRestroBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
