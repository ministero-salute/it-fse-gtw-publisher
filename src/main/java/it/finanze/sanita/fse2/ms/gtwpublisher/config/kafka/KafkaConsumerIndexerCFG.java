/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class KafkaConsumerIndexerCFG  {

	/**
	 *	Kafka consumer properties.
	 */
	@Autowired
	private KafkaConsumerPropertiesCFG kafkaConsumerPropCFG;

	@Autowired
	private KafkaPropertiesCFG kafkaProps;

	@Autowired
	private KafkaTopicCFG kafkaTopicCFG;

	/**
	 * Configurazione consumer.
	 * 
	 * @return	configurazione consumer
	 */
	@Bean
	public Map<String, Object> consumerConfigsIndexer() {
		Map<String, Object> props = new HashMap<>();

		props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConsumerPropCFG.getClientIdIndexer());
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerPropCFG.getConsumerBootstrapServers());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerPropCFG.getConsumerGroupIdIndexer());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConsumerPropCFG.getConsumerKeyDeserializer());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConsumerPropCFG.getConsumerValueDeserializer());
		props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, kafkaConsumerPropCFG.getIsolationLevel());
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConsumerPropCFG.getAutoCommit());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerPropCFG.getAutoOffsetReset());


		if(!StringUtility.isNullOrEmpty(kafkaProps.getProtocol())) {
			props.put("security.protocol", kafkaProps.getProtocol());
		}

		if(!StringUtility.isNullOrEmpty(kafkaProps.getMechanism())) {
			props.put("sasl.mechanism", kafkaProps.getMechanism());
		}

		if(!StringUtility.isNullOrEmpty(kafkaProps.getConfigJaas())) {
			props.put("sasl.jaas.config", kafkaProps.getConfigJaas());
		}

		if(!StringUtility.isNullOrEmpty(kafkaProps.getTrustoreLocation())) {
			props.put("ssl.truststore.location", kafkaProps.getTrustoreLocation());
		}

		if(!StringUtility.isNullOrEmpty(String.valueOf(kafkaProps.getTrustorePassword()))) {
			props.put("ssl.truststore.password", String.valueOf(kafkaProps.getTrustorePassword()));
		}

		return props;
	}

	/**
	 * Consumer factory.
	 * 
	 * @return	factory
	 */
	@Bean
	public ConsumerFactory<String, String> consumerFactoryIndexer() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigsIndexer());
	}

	/**
	 * Factory with dead letter configuration.
	 * 
	 * @return	factory
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaIndexerListenerDeadLetterContainerFactory(final @Qualifier("notxkafkadeadtemplate") KafkaTemplate<Object, Object> deadLetterKafkaTemplate) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactoryIndexer());
		factory.getContainerProperties().setDeliveryAttemptHeader(true);
		// Definizione nome topic deadLetter
		log.debug("TOPIC definition: " + kafkaTopicCFG.getIndexerPublisherDeadLetterTopic());
		DeadLetterPublishingRecoverer dlpr = new DeadLetterPublishingRecoverer(deadLetterKafkaTemplate, (record, ex) -> new TopicPartition(kafkaTopicCFG.getIndexerPublisherDeadLetterTopic(), -1));

		// Set classificazione errori da gestire per la deadLetter.
		DefaultErrorHandler sceh = new DefaultErrorHandler(dlpr, new FixedBackOff(FixedBackOff.DEFAULT_INTERVAL, FixedBackOff.UNLIMITED_ATTEMPTS));

		log.debug("Kafka dead letter classification");
		setClassification(sceh);

		// da eliminare se non si volesse gestire la dead letter
		factory.setCommonErrorHandler(sceh); 

		return factory;
	}

	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactoryIndexer() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactoryIndexer());

		return factory;
	}

	protected void setClassification(final DefaultErrorHandler sceh) {
		List<Class<? extends Exception>> out = getExceptionsConfig();
		for (Class<? extends Exception> ex : out) {
			log.warn("Found a non retryable Exception: {}", ex.getCanonicalName());
			sceh.addNotRetryableExceptions(ex);
		}
	}
	/**
	 * @return	exceptions list
	 */
	@SuppressWarnings("unchecked")
	protected List<Class<? extends Exception>> getExceptionsConfig() {
		List<Class<? extends Exception>> out = new ArrayList<>();
		String temp = null;
		try {
			for (String excs : kafkaConsumerPropCFG.getDeadLetterExceptions()) {
				temp = excs;
				Class<? extends Exception> s = (Class<? extends Exception>) Class.forName(excs, false, Thread.currentThread().getContextClassLoader());
				out.add(s);
			}
		} catch (Exception e) {
			log.error("Error retrieving the exception with fully qualified name: <{}>", temp);
			log.error("Error : ", e);
		}

		return out;
	}

}
