package com.lera.catalog.integration

import io.restassured.RestAssured
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
abstract class BaseIntegrationTest {

    companion object {
        val PSQL_CONTAINER: PostgreSQLContainer<*> = PostgreSQLContainer<Nothing>("postgres:16")
        val KAFKA_CONTAINER: KafkaContainer = KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"))

        init {
            PSQL_CONTAINER.start()
            KAFKA_CONTAINER.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun jdbcProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", PSQL_CONTAINER::getJdbcUrl)
            registry.add("spring.datasource.username", PSQL_CONTAINER::getUsername)
            registry.add("spring.datasource.password", PSQL_CONTAINER::getPassword)
            registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers)
        }
    }

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @LocalServerPort
    var serverPort: Int = 0

    @BeforeEach
    fun beforeEach() {
        RestAssured.port = serverPort
    }

    @AfterEach
    fun cleanUp() {
        jdbcTemplate.execute("truncate table good cascade;")
        jdbcTemplate.execute("ALTER SEQUENCE good_id_seq RESTART WITH 1;")
    }

    protected fun createTestConsumer(): KafkaConsumer<String, String> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to KAFKA_CONTAINER.bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "test-group-${UUID.randomUUID()}",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
        return KafkaConsumer(props)
    }
}
