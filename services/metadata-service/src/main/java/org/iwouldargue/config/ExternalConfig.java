package org.iwouldargue.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
@ConfigurationProperties(prefix = "external")
@Data
@Validated
public class ExternalConfig {
    @NotNull
    private String mediaServiceUrl;
    @NotNull
    private String notificationServiceUrl;
    @NotNull
    private String livekitUrl;

    @Bean
    public RestTemplate mediaRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(mediaServiceUrl);
        restTemplate.setUriTemplateHandler(factory);

        return restTemplate;
    }

    @Bean
    public RestTemplate notificationRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(notificationServiceUrl);
        restTemplate.setUriTemplateHandler(factory);

        return restTemplate;
    }
}
