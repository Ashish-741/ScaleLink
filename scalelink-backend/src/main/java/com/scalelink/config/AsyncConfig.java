package com.scalelink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration
 *
 * WHY ASYNC?
 * When a user clicks a short URL, two things happen:
 * 1. Redirect the user to the original URL (FAST — user sees this)
 * 2. Record the click event in the database (SLOW — user doesn't care)
 *
 * Without async: redirect waits for click recording → ~50ms delay
 * With async: redirect happens immediately, click records in background
 *
 * WHAT IS A THREAD POOL?
 * Instead of creating a new thread for every async task (expensive),
 * we maintain a pool of reusable threads. Tasks are queued and
 * executed by available threads from the pool.
 *
 * CONFIGURATION EXPLAINED:
 * - corePoolSize: 5 → always have 5 threads ready
 * - maxPoolSize: 10 → can grow to 10 under load
 * - queueCapacity: 500 → queue up to 500 tasks before rejecting
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("scalelink-async-");
        executor.initialize();
        return executor;
    }
}
