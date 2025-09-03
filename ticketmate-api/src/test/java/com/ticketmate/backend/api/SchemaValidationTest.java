package com.ticketmate.backend.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = SchemaVerifyApplication.class)
@ActiveProfiles("schema-verify")
public class SchemaValidationTest {

  @Test
  void contextLoad() {

  }
}
