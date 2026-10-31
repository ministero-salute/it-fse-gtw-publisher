package it.finanze.sanita.fse2.ms.gtwpublisher.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.util.ReflectionTestUtils;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.*;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BlockingEdsException;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.ProfileUtility;

@Tag("ISOLATION")
class KafkaConfigurationIsolationTest {
    private KafkaPropertiesCFG security(boolean enabled) {
        KafkaPropertiesCFG cfg = new KafkaPropertiesCFG();
        cfg.setProtocol(enabled ? "SASL_SSL" : "");
        cfg.setMechanism(enabled ? "PLAIN" : "");
        cfg.setConfigJaas(enabled ? "test-jaas" : "");
        cfg.setTrustoreLocation(enabled ? "/test/store" : "");
        cfg.setTrustorePassword(enabled ? "secret".toCharArray() : new char[0]);
        return cfg;
    }

    private void assertSecurity(Map<String, Object> props, boolean enabled) {
        if (enabled) {
            assertEquals("SASL_SSL", props.get("security.protocol"));
            assertEquals("PLAIN", props.get("sasl.mechanism"));
            assertEquals("test-jaas", props.get("sasl.jaas.config"));
            assertEquals("/test/store", props.get("ssl.truststore.location"));
            assertEquals("secret", props.get("ssl.truststore.password"));
        } else {
            assertFalse(props.containsKey("security.protocol"));
            assertFalse(props.containsKey("sasl.mechanism"));
            assertFalse(props.containsKey("sasl.jaas.config"));
            assertFalse(props.containsKey("ssl.truststore.location"));
            assertFalse(props.containsKey("ssl.truststore.password"));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void producerFactoriesPreserveDeliveryAndSecuritySettingsWithoutConnecting(boolean secured) {
        KafkaProducerPropertiesCFG properties = new KafkaProducerPropertiesCFG();
        properties.setClientId("publisher");
        properties.setRetries(3);
        properties.setProducerBootstrapServers("unused.invalid:9092");
        properties.setKeySerializer("org.apache.kafka.common.serialization.StringSerializer");
        properties.setValueSerializer("org.apache.kafka.common.serialization.StringSerializer");
        properties.setTransactionalId("transaction");
        properties.setAck("all");
        properties.setIdempotence(true);
        KafkaProducerCFG cfg = new KafkaProducerCFG();
        ReflectionTestUtils.setField(cfg, "kafkaPropCFG", security(secured));
        ReflectionTestUtils.setField(cfg, "kafkaProducerPropCFG", properties);
        Map<String, Object> tx = cfg.producerConfigs();
        assertEquals(3, tx.get(ProducerConfig.RETRIES_CONFIG));
        assertEquals("all", tx.get(ProducerConfig.ACKS_CONFIG));
        assertEquals(true, tx.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
        assertTrue(tx.get(ProducerConfig.TRANSACTIONAL_ID_CONFIG).toString().endsWith("-transaction"));
        assertSecurity(tx, secured);
        Map<String, Object> noTx = cfg.producerWithoutTransactionConfigs();
        assertEquals("publisher-noTx", noTx.get(ProducerConfig.CLIENT_ID_CONFIG));
        assertFalse(noTx.containsKey(ProducerConfig.TRANSACTIONAL_ID_CONFIG));
        assertSecurity(noTx, secured);
        assertTrue(cfg.producerFactory().transactionCapable());
        assertFalse(cfg.producerFactoryWithoutTransaction().transactionCapable());
        assertFalse(cfg.producerDeadFactory().transactionCapable());
        assertNotNull(cfg.txKafkaTemplate());
        assertNotNull(cfg.notxKafkaTemplate());
        assertNotNull(cfg.noTxKafkaDeadTemplate());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void consumerFactoriesPreserveOffsetsIsolationAndSecurityWithoutStartingListeners(boolean secured) {
        KafkaConsumerPropertiesCFG properties = new KafkaConsumerPropertiesCFG();
        properties.setClientIdIndexer("indexer");
        properties.setConsumerGroupIdIndexer("group");
        properties.setConsumerBootstrapServers("unused.invalid:9092");
        properties.setConsumerKeyDeserializer("org.apache.kafka.common.serialization.StringDeserializer");
        properties.setConsumerValueDeserializer("org.apache.kafka.common.serialization.StringDeserializer");
        properties.setIsolationLevel("read_committed");
        properties.setAutoCommit("false");
        properties.setAutoOffsetReset("earliest");
        properties.setDeadLetterExceptions(List.of(BlockingEdsException.class.getName()));
        KafkaTopicCFG topics = new KafkaTopicCFG();
        topics.setIndexerPublisherDeadLetterTopic("dead-letter");
        KafkaConsumerIndexerCFG cfg = new KafkaConsumerIndexerCFG();
        ReflectionTestUtils.setField(cfg, "kafkaConsumerPropCFG", properties);
        ReflectionTestUtils.setField(cfg, "kafkaProps", security(secured));
        ReflectionTestUtils.setField(cfg, "kafkaTopicCFG", topics);
        Map<String, Object> props = cfg.consumerConfigsIndexer();
        assertEquals("group", props.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals("read_committed", props.get(ConsumerConfig.ISOLATION_LEVEL_CONFIG));
        assertEquals("false", props.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        assertEquals("earliest", props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        assertSecurity(props, secured);
        assertEquals(props, cfg.consumerFactoryIndexer().getConfigurationProperties());
        assertNotNull(cfg.kafkaListenerContainerFactoryIndexer());
        assertNotNull(cfg.kafkaIndexerListenerDeadLetterContainerFactory(mock(KafkaTemplate.class)));
        DefaultErrorHandler handler = mock(DefaultErrorHandler.class);
        ReflectionTestUtils.invokeMethod(cfg, "setClassification", handler);
        verify(handler).addNotRetryableExceptions(BlockingEdsException.class);
        properties.setDeadLetterExceptions(List.of(BlockingEdsException.class.getName(), "missing.Exception"));
        assertEquals(List.of(BlockingEdsException.class), ReflectionTestUtils.invokeMethod(cfg, "getExceptionsConfig"));
    }

    @Test
    void exceptionClassificationUsesExactTypesAndTemporaryTakesPrecedence() {
        KafkaConsumerPropertiesCFG cfg = new KafkaConsumerPropertiesCFG();
        cfg.setDeadLetterExceptions(List.of(BlockingEdsException.class.getCanonicalName()));
        cfg.setTemporaryExceptions(List.of(IllegalStateException.class.getCanonicalName()));
        assertEquals(EventStatusEnum.BLOCKING_ERROR, cfg.asExceptionType(new BlockingEdsException("bad")).orElseThrow());
        assertEquals(EventStatusEnum.NON_BLOCKING_ERROR, cfg.asExceptionType(new IllegalStateException()).orElseThrow());
        assertTrue(cfg.asExceptionType(new Exception()).isEmpty());
        cfg.setTemporaryExceptions(cfg.getDeadLetterExceptions());
        assertEquals(EventStatusEnum.NON_BLOCKING_ERROR, cfg.asExceptionType(new BlockingEdsException("bad")).orElseThrow());
        assertDoesNotThrow(() -> cfg.deadLetterHelper(new Exception("outer", new IllegalStateException("inner"))));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void prefixesAllTopicsOnlyForTestProfile(boolean test) {
        ProfileUtility profile = mock(ProfileUtility.class);
        when(profile.isTestProfile()).thenReturn(test);
        KafkaTopicCFG cfg = new KafkaTopicCFG();
        cfg.setIndexerPublisherTopic("input");
        cfg.setIndexerPublisherDeadLetterTopic("dead");
        cfg.setStatusManagerTopic("status");
        ReflectionTestUtils.setField(cfg, "profileUtility", profile);
        cfg.afterInit();
        String prefix = test ? Constants.Profile.TEST_PREFIX : "";
        assertEquals(prefix + "input", cfg.getIndexerPublisherTopic());
        assertEquals(prefix + "dead", cfg.getIndexerPublisherDeadLetterTopic());
        assertEquals(prefix + "status", cfg.getStatusManagerTopic());
    }

    @ParameterizedTest
    @ValueSource(strings = {"prod", "test", "dev"})
    void adminClientReceivesExpectedSecurityPropertiesWithoutNetwork(String profileName) {
        KafkaPropertiesCFG cfg = security(true);
        cfg.setProducerBootstrapServers("unused.invalid:9092");
        ProfileUtility profile = mock(ProfileUtility.class);
        when(profile.isTestProfile()).thenReturn(profileName.equals("test"));
        when(profile.isDevOrDockerProfile()).thenReturn(profileName.equals("dev"));
        ReflectionTestUtils.setField(cfg, "profileUtility", profile);
        AdminClient admin = mock(AdminClient.class);
        try (var factory = mockStatic(AdminClient.class)) {
            factory.when(() -> AdminClient.create(any(Properties.class))).thenReturn(admin);
            assertSame(admin, cfg.client());
            factory.verify(() -> AdminClient.create(argThat((Properties p) ->
                    "unused.invalid:9092".equals(p.get("bootstrap.servers"))
                    && (profileName.equals("prod") ? "SASL_SSL".equals(p.get("security.protocol")) : !p.containsKey("security.protocol")))));
        }
    }
}
