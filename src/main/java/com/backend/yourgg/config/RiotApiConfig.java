package com.backend.yourgg.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RiotApiConfig {

    @Value("${riot.api.key}")
    private String apiKey;

    @Value("${riot.api.base-url}")
    private String baseUrl;
}
