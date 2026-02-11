package org.iwouldargue.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "auth.jwt")
@Data
@Validated
public class AuthConfig {
    @NotNull
    private String secretKey;
    @NotNull
    private int expirationSeconds;
}
