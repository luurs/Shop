package com.lera.orders.configuration;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogClientConfig {

    @Bean
    public Retryer catalogRetryer() {
        return new Retryer.Default(
                100,
                1000,
                3
        );
    }
}
