package it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka;

import java.util.HashMap;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class KafkaConsumerIndexerCFG extends KafkaConsumerCFG {

	/**
	 *	Kafka consumer properties.
	 */
	@Autowired
	private KafkaConsumerPropertiesCFG kafkaConsumerPropCFG;

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

		log.info("CLIENT_ID_CONFIG: " + kafkaConsumerPropCFG.getClientIdIndexer());
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConsumerPropCFG.getClientIdIndexer());
		
		log.info("BOOTSTRAP_SERVERS_CONFIG: " + kafkaConsumerPropCFG.getConsumerBootstrapServers());
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerPropCFG.getConsumerBootstrapServers());
		
		log.info("GROUP_ID_CONFIG: " + kafkaConsumerPropCFG.getConsumerGroupIdIndexer());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerPropCFG.getConsumerGroupIdIndexer());
		
		addCommonsProperties(props);

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
	 * @param deadLetterKafkaTemplate
	 * @return	factory
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaIndexerListenerDeadLetterContainerFactory(final @Qualifier("notxkafkadeadtemplate") KafkaTemplate<Object, Object> deadLetterKafkaTemplate) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactoryIndexer());
		
		// Definizione nome topic deadLetter
		log.info("TOPIC: " + kafkaTopicCFG.getIndexerPublisherDeadLetterTopic());
		DeadLetterPublishingRecoverer dlpr = new DeadLetterPublishingRecoverer(deadLetterKafkaTemplate, (record, ex) -> new TopicPartition(kafkaTopicCFG.getIndexerPublisherDeadLetterTopic(), -1));
		
		// Set classificazione errori da gestire per la deadLetter.
		DefaultErrorHandler sceh = new DefaultErrorHandler(dlpr, new FixedBackOff(FixedBackOff.DEFAULT_INTERVAL, FixedBackOff.UNLIMITED_ATTEMPTS));
		
		log.info("setClassification - kafkaIndexerListenerDeadLetterContainerFactory: ");
		setClassification(sceh);
		
		// da eliminare se non si volesse gestire la dead letter
		factory.setCommonErrorHandler(sceh); 

		return factory;
	}
	
	
	
	/**
	 * Default Container factory.
	 * 
	 * @param kafkaTemplate	templete
	 * @return				factory
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactoryIndexer(final @Qualifier("notxkafkatemplate") KafkaTemplate<String, String> kafkaTemplate) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactoryIndexer());
		
		return factory;
	}
}
