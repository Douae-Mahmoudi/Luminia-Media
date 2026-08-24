package com.example.notification.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConsumerConfigTest {

    private KafkaConsumerConfig kafkaConsumerConfig;
    private final String BOOTSTRAP_SERVERS = "localhost:9092";

    @BeforeEach
    void setUp() {
        kafkaConsumerConfig = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(kafkaConsumerConfig, "bootstrapServers", BOOTSTRAP_SERVERS);
    }

    @Test
    void shouldCreateMainContainerFactoryWithCorrectConfig() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                kafkaConsumerConfig.kafkaListenerContainerFactory();

        assertThat(factory).isNotNull();

        ConsumerFactory<String, Object> consumerFactory = (ConsumerFactory<String, Object>) factory.getConsumerFactory();
        Map<String, Object> props = consumerFactory.getConfigurationProperties();

        assertThat(props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo(BOOTSTRAP_SERVERS);
        assertThat(props.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo("notif-group");
        assertThat(props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)).isEqualTo("earliest");
    }

    @Test
    void shouldHaveSpecificConcurrencySet() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                kafkaConsumerConfig.kafkaListenerContainerFactory();

        Integer concurrency = (Integer) ReflectionTestUtils.getField(factory, "concurrency");
        assertThat(concurrency).isEqualTo(2);
    }

    @Test
    void shouldCreateTypedFactories() {
        assertThat(kafkaConsumerConfig.mediaLikedContainerFactory()).isNotNull();
        assertThat(kafkaConsumerConfig.mediaStatusContainerFactory()).isNotNull();
        assertThat(kafkaConsumerConfig.commentContainerFactory()).isNotNull();
    }
}