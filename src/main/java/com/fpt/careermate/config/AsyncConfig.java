package com.fpt.careermate.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * Configure thread pool for general async operations
     * - Core pool size: 5 threads always active
     * - Max pool size: 10 threads maximum
     * - Queue capacity: 25 tasks waiting
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        return executor;
    }

    /**
     * Configure thread pool specifically for email async operations
     * Separate pool to prevent email sending from blocking other async tasks
     * - Core pool size: 3 threads (enough for typical email volume)
     * - Max pool size: 5 threads maximum
     * - Queue capacity: 50 tasks (emails can be queued longer)
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Email-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120); // Email might take longer
        executor.initialize();

        return executor;
    }

    /**
     * Configure thread pool specifically for Weaviate async operations
     * Separate pool for vector DB operations to prevent blocking
     * - Core pool size: 2 threads (Weaviate operations are heavier)
     * - Max pool size: 4 threads maximum
     * - Queue capacity: 20 tasks
     */
    @Bean(name = "weaviateTaskExecutor")
    public Executor weaviateTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("Weaviate-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(90); // Vector operations might take time
        executor.initialize();

        return executor;
    }

    @Bean(name = "roadmapTaskExecutor")
    public Executor roadmapTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("Roadmap-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }

}

