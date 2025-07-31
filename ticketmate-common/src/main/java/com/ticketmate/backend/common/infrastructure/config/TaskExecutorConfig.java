package com.ticketmate.backend.common.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TaskExecutionProperties.class)
public class TaskExecutorConfig {

  private final TaskExecutionProperties properties;

  @Primary
  @Bean("applicationTaskExecutor")
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(properties.getPool().getCoreSize());
    executor.setMaxPoolSize(properties.getPool().getMaxSize());
    executor.setQueueCapacity(properties.getPool().getQueueCapacity());
    executor.initialize();
    return executor;
  }
}