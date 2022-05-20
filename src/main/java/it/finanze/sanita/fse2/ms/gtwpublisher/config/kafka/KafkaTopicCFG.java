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
		
}
