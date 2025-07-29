package com.ticketmate.backend.global.config.beans;

import com.ticketmate.backend.global.config.properties.CoolSmsProperties;
import com.ticketmate.backend.global.config.properties.FirebaseProperties;
import com.ticketmate.backend.global.config.properties.GoogleGenAiProperties;
import com.ticketmate.backend.global.config.properties.RabbitMqProperties;
import com.ticketmate.backend.global.config.properties.S3Properties;
import com.ticketmate.backend.global.config.properties.TaskExecutorProperties;
import com.ticketmate.backend.global.config.properties.WebSocketProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    CoolSmsProperties.class,
    FirebaseProperties.class,
    GoogleGenAiProperties.class,
    RabbitMqProperties.class,
    S3Properties.class,
    TaskExecutorProperties.class,
    WebSocketProperties.class
})
public class PropertiesConfig {

}
