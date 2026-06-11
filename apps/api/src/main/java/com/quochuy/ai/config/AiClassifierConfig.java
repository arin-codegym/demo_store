package com.quochuy.ai.config;

import com.quochuy.helper.AiClassifierProperties;
import com.quochuy.helper.AiExternalSearchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AiClassifierProperties.class, AiExternalSearchProperties.class})
public class AiClassifierConfig {
}
