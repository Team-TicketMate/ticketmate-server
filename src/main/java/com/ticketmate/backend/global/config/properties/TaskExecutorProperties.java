package com.ticketmate.backend.global.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@AllArgsConstructor
@ConfigurationProperties(prefix = "spring.task.execution.pool")
public class TaskExecutorProperties {

  private final int coreSize;

  private final int maxSize;

  private final int queueCapacity;

}
