package com.ticketmate.backend.api;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.ticketmate.backend")
public class SchemaVerifyApplication {

}
