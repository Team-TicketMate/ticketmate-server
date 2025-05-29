package com.ticketmate.backend.util.config;

import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class FakerConfig {

    @Bean
    public Faker koFaker() {
        return new Faker(new Locale("ko", "KR"));
    }

    @Bean
    public Faker enFaker() {
        return new Faker(new Locale("en"));
    }
}
