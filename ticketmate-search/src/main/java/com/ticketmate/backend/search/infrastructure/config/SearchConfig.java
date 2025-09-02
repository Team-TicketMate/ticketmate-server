package com.ticketmate.backend.search.infrastructure.config;

import com.ticketmate.backend.search.infrastructure.properties.SearchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SearchProperties.class)
public class SearchConfig {

}
