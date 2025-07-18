package com.ticketmate.backend.global.config.beans;

import com.ticketmate.backend.global.config.properties.TaskExecutorProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TaskExecutorProperties.class)
public class TaskExecutorConfig {

  private final TaskExecutorProperties properties;

  @Primary
  @Bean("applicationTaskExecutor")
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(properties.getCoreSize());
    executor.setMaxPoolSize(properties.getMaxSize());
    executor.setQueueCapacity(properties.getQueueCapacity());
    executor.initialize();
    return executor;
  }
}