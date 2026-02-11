package org.iwouldargue.gateway;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "services")
@Data
public class GatewayConfig {

    private String metadataServiceUrl;
    private String notificationServiceUrl;
    private String mediaServiceUrl;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r
                        .path("/api/v1/connect/**")
                        .or().path("/api/v1/manage/**")
                        .uri(notificationServiceUrl)
                )
                .route(r -> r
                        .path("/api/v1/tokens/**")
                        .or().path("/api/v1/participants/**")
                        .uri(mediaServiceUrl)
                )
                .route(r -> r
                        .path("/api/v1/**")
                        .uri(metadataServiceUrl)
                ).build();
    }
}
