package com.lera.catalog.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@ConditionalOnProperty(value = ["outbox.scheduler.enabled"], havingValue = "true", matchIfMissing = true)
class SchedulerConfig {
}