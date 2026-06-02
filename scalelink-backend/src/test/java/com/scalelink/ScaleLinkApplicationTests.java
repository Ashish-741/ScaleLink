package com.scalelink;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the application context loads successfully.
 * If this test passes, it means:
 * - All beans are created without errors
 * - All configurations are valid
 * - Database connection works
 * - Redis connection works
 * - No circular dependencies
 */
@SpringBootTest
@ActiveProfiles("dev")
class ScaleLinkApplicationTests {

    @Test
    void contextLoads() {
        // If the application context loads without exceptions, the test passes.
        // This is the most basic "smoke test" — does the app start at all?
    }
}
