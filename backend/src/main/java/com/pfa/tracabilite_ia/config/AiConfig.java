package com.pfa.tracabilite_ia.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({OpenRouterProperties.class, GroqProperties.class})
public class AiConfig {
}
