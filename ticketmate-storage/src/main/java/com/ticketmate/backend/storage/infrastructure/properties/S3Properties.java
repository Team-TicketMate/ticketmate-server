package com.ticketmate.backend.storage.infrastructure.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cloud.aws")
public record S3Properties(
    @Valid S3 s3,
    @Valid Credentials credentials,
    @Valid Region region,
    @Valid Stack stack
) {

  public record S3(
      @NotBlank String bucket,
      @NotBlank String domain,
      @Valid Path path
  ) {

  }

  public record Path(
      @NotBlank String member,
      @NotBlank String concertHall,
      @NotBlank String concert,
      @NotBlank String portfolio,
      @NotBlank String chat,
      @NotBlank String review,
      @NotBlank String fulfillmentForm
  ) {

  }

  public record Credentials(
      @NotBlank String accessKey,
      @NotBlank String secretKey
  ) {

  }

  public record Region(
      @NotBlank String staticRegion,
      boolean auto
  ) {

  }

  public record Stack(boolean auto) {

  }
}
