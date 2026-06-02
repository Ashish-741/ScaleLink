package com.scalelink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * ScaleLink Application Entry Point
 *
 * WHAT IS @SpringBootApplication?
 * It's a convenience annotation that combines THREE annotations:
 *
 * 1. @Configuration — This class can define @Bean methods (factory methods
 *    that create objects Spring manages). Think of it as a "recipe book"
 *    telling Spring how to create certain objects.
 *
 * 2. @EnableAutoConfiguration — Spring Boot's magic. It looks at the
 *    dependencies in pom.xml and automatically configures them:
 *    - Found PostgreSQL driver? → Configure a DataSource
 *    - Found Spring Web? → Configure an embedded Tomcat server
 *    - Found Spring Security? → Configure default security
 *    You CAN override any auto-configuration with your own @Bean.
 *
 * 3. @ComponentScan — Scans this package and all sub-packages for classes
 *    annotated with @Component, @Service, @Repository, @Controller, etc.
 *    and registers them as Spring-managed beans (objects).
 *
 * WHAT IS A "BEAN"?
 * A bean is simply a Java object that Spring creates and manages for you.
 * Instead of doing `new UserService()` everywhere, Spring creates ONE
 * instance and "injects" it wherever it's needed. This is called
 * DEPENDENCY INJECTION — the most important concept in Spring.
 *
 * WHAT IS @EnableAsync?
 * Allows methods annotated with @Async to run in a separate thread.
 * We use this for recording click events asynchronously — the redirect
 * response is sent immediately while the click is recorded in the background.
 */
@SpringBootApplication
@EnableAsync
public class ScaleLinkApplication {

    public static void main(String[] args) {
        // SpringApplication.run() does everything:
        // 1. Creates the Spring application context (the container for all beans)
        // 2. Scans for components and registers them
        // 3. Auto-configures based on dependencies
        // 4. Starts the embedded Tomcat server
        // 5. Runs Flyway migrations (if configured)
        // 6. Connects to PostgreSQL and Redis
        SpringApplication.run(ScaleLinkApplication.class, args);
    }
}
