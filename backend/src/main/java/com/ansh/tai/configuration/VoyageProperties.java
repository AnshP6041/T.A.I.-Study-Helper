package com.ansh.tai.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "voyage")
public record VoyageProperties(String apiKey, String model) {}