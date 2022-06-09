package it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 *	@author vincenzoingenito
 *
 *	Kafka topic configuration.
 */
@Data
@Component
public class KafkaTopicCFG {

	/**
	 * Topic.
	 */
	@Value("${kafka.indexer-publisher.topic}")
	private String indexerPublisherTopic;
	
	/**
	 * Dead letter topic.
	 */
	@Value("${kafka.indexer-publisher.deadletter.topic}")
	private String indexerPublisherDeadLetterTopic;


	/**
	 * Topic.
	 */
	@Value("${kafka.dispatcher-publisher.topic}")
	private String dispatcherPublisherTopic;
	
	/**
	 * Dead letter topic.
	 */
	@Value("${kafka.dispatcher-publisher.deadletter.topic}")
	private String dispatcherPublisherDeadLetterTopic;
	
	
	/**
	 * Status Manager topic.
	 */
	@Value("${kafka.statusmanager.topic}")
	private String statusManagerTopic;
		
}
