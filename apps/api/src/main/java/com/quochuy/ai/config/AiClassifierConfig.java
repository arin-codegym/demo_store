package com.quochuy.ai.config;

import com.quochuy.helper.AiClassifierProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiClassifierProperties.class)
public class AiClassifierConfig {
}
