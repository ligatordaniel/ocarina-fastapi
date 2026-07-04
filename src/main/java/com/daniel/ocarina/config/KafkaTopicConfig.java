package com.daniel.ocarina.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    private final OcarinaProperties properties;

    public KafkaTopicConfig(OcarinaProperties properties) {
        this.properties = properties;
    }

    @Bean
    public NewTopic downloadRequestsTopic() {
        return TopicBuilder.name(properties.getKafka().getRequestsTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
