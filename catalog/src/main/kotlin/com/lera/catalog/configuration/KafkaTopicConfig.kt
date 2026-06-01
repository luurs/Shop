package com.lera.catalog.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {

    @Value("\${spring.kafka.topics.invalidate-goods-cache}")
    private lateinit var invalidateGoodsCacheTopic: String

    @Bean
    fun invalidateGoodsCacheTopic(): NewTopic = TopicBuilder.name(invalidateGoodsCacheTopic)
        .partitions(1)
        .replicas(1)
        .build()
}
