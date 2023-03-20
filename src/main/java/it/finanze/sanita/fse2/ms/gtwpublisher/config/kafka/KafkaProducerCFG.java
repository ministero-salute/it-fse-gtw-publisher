/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 *
 *	Kafka producer configuration.
 */
@Configuration
@Slf4j
public class KafkaProducerCFG {

	public static final int MAX_ATTEMPT = 5;

	/**
	 *	Kafka properties.
	 */
	@Autowired
	private KafkaPropertiesCFG kafkaPropCFG;

	/**
	 *	Kafka producer properties.
	 */
	@Autowired
	private KafkaProducerPropertiesCFG kafkaProducerPropCFG;

	/** 
	 *  Kafka producer configurazione.
	 */
	@Bean 
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();

		InetAddress id = getLocalHost();

		props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaProducerPropCFG.getClientId() + "-tx" + "-" + id );
		props.put(ProducerConfig.RETRIES_CONFIG, kafkaProducerPropCFG.getRetries());
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerPropCFG.getProducerBootstrapServers());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProducerPropCFG.getKeySerializer());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProducerPropCFG.getValueSerializer());
		props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, id + "-" + kafkaProducerPropCFG.getTransactionalId());
		props.put(ProducerConfig.ACKS_CONFIG,kafkaProducerPropCFG.getAck());
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,kafkaProducerPropCFG.getIdempotence());

		//SSL
		if(!StringUtility.isNullOrEmpty(kafkaPropCFG.getProtocol())) {
			props.put("security.protocol", kafkaPropCFG.getProtocol());
		}

		if(!StringUtility.isNullOrEmpty(kafkaPropCFG.getMechanism())) {
			props.put("sasl.mechanism", kafkaPropCFG.getMechanism());
		}

		if(!StringUtility.isNullOrEmpty(kafkaPropCFG.getConfigJaas())) {
			props.put("sasl.jaas.config", kafkaPropCFG.getConfigJaas());
		}

		if(!StringUtility.isNullOrEmpty(kafkaPropCFG.getTrustoreLocation())) {
			props.put("ssl.truststore.location", kafkaPropCFG.getTrustoreLocation());  
		}

		if(!StringUtility.isNullOrEmpty(String.valueOf(kafkaPropCFG.getTrustorePassword()))) {
			props.put("ssl.truststore.password", String.valueOf(kafkaPropCFG.getTrustorePassword())); 
		}

		return props;
	}

	private InetAddress getLocalHost() {
		InetAddress id = null;
		try {
			id = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			log.error("Errore durante il recupero InetAddress.getLocalHost()", e);
		}
		return id;
	}

	/**
	 * Transactional producer.
	 */
	@Bean
	@Qualifier("txkafkatemplateFactory") 
	public ProducerFactory<String, String> producerFactory() {
		log.debug("Initialization of transaction Factory");
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	/**
	 *  Kafka template.
	 */
	@Bean
	@Qualifier("txkafkatemplate") 
	public KafkaTemplate<String, String> txKafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	/**
	 * Non transactional producer config.
	 */
	@Bean 
	public Map<String, Object> producerWithoutTransactionConfigs() {
		Map<String, Object> props = new HashMap<>();

		props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaProducerPropCFG.getClientId()+ "-noTx");
		props.put(ProducerConfig.RETRIES_CONFIG, kafkaProducerPropCFG.getRetries());
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerPropCFG.getProducerBootstrapServers());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProducerPropCFG.getKeySerializer());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProducerPropCFG.getValueSerializer());

		//SSL
		if(!StringUtility.isNullOrEmpty(kafkaPropCFG.getProtocol())) {
			props.put("security.protocol", kafkaPropCFG.getProtocol());
		}
		
		if(!StringUtility.isNullOrEmpty(kafkaPropCFG.getMechanism())) {
			props.put("sasl.mechanism", kafkaPropCFG.getMechanism());
		}
		
		if(!StringUtility.isNullOrEmpty(kafkaPropCFG.getConfigJaas())) {
			props.put("sasl.jaas.config", kafkaPropCFG.getConfigJaas());
		}
		
		if(!StringUtility.isNullOrEmpty(kafkaPropCFG.getTrustoreLocation())) {
			props.put("ssl.truststore.location", kafkaPropCFG.getTrustoreLocation());
		}
		
		if(!StringUtility.isNullOrEmpty(String.valueOf(kafkaPropCFG.getTrustorePassword()))) {
			props.put("ssl.truststore.password", String.valueOf(kafkaPropCFG.getTrustorePassword())); 
		}

		return props;
	}

	/**
	 * Non transactional producer.
	 */ 
	@Bean
	@Qualifier("notxkafkatemplateFactory") 
	public ProducerFactory<String, String> producerFactoryWithoutTransaction() {
		log.info("Initialization of transactional Factory");
		return new DefaultKafkaProducerFactory<>(producerWithoutTransactionConfigs());
	}

	/**
	 * Non transactional kafka template.
	 */ 
	@Bean
	@Qualifier("notxkafkatemplate") 
	public KafkaTemplate<String, String> notxKafkaTemplate() {
		return new KafkaTemplate<>(producerFactoryWithoutTransaction());
	}


	/**
	 * Facotry dead producer.
	 * 
	 * @return	factory dead producer.
	 */
	@Bean
	public ProducerFactory<Object, Object> producerDeadFactory() {
		return new DefaultKafkaProducerFactory<>(producerWithoutTransactionConfigs());
	}

	/**
	 * Kafka template dead letter.
	 *
	 * @return	Kafka template
	 */
	@Bean
	@Qualifier("notxkafkadeadtemplate")
	public KafkaTemplate<Object, Object> noTxKafkaDeadTemplate() {
		return new KafkaTemplate<>(producerDeadFactory());
	}

}