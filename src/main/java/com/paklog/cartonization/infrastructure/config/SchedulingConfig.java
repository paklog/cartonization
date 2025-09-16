package com.paklog.cartonization.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);

    @Value("${app.scheduling.pool-size:5}")
    private int poolSize;

    @Value("${app.scheduling.thread-name-prefix:scheduling-}")
    private String threadNamePrefix;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(threadNamePrefix);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        
        // Configure error handling
        scheduler.setErrorHandler(throwable -> {
            log.error("Scheduled task execution failed", throwable);
        });

        scheduler.initialize();

        log.info("Task scheduler configured with pool size: {}", poolSize);
        return scheduler;
    }
}