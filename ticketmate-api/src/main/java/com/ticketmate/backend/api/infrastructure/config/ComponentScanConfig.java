package com.ticketmate.backend.api.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "com.ticketmate.backend.*"
})
public class ComponentScanConfig {

}
