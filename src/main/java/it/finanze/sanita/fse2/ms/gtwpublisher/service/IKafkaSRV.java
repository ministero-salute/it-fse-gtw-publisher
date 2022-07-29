package it.finanze.sanita.fse2.ms.gtwpublisher.service;

import java.io.Serializable;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.messaging.MessageHeaders;

import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;
 

public interface IKafkaSRV extends Serializable {

	/**
	 * Send message over kafka topic
	 * @param topic
	 * @param key
	 * @param value
	 * @param trans
	 * @return
	 */
	RecordMetadata sendMessage(String topic, String key, String value, boolean trans);
 
	/**
	 * Kafka listener for Indexer communications in low priority
	 * @param cr
	 * @param messageHeaders
	 */
	void lowPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders);

	/**
	 * Kafka listener for Indexer communications in medium priority
	 * @param cr
	 * @param messageHeaders
	 */
	void mediumPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders);

	/**
	 * Kafka listener for Indexer communications in high priority
	 * @param cr
	 * @param messageHeaders
	 */
	void highPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders);

	/**
	 * Kafka listener for Dispatcher communications for TSFeeding in low priority
	 * @param cr
	 * @param messageHeaders
	 */
	void lowPriorityListenerDispatcher(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders);

	/**
	 * Kafka listener for Dispatcher communications for TSFeeding in medium priority
	 * @param cr
	 * @param messageHeaders
	 */
	void mediumPriorityListenerDispatcher(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders);

	/**
	 * Kafka listener for Dispatcher communications for TSFeeding in high priority
	 * @param cr
	 * @param messageHeaders
	 */
	void highPriorityListenerDispatcher(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders);

	/**
	 * Send status message to respective topic
	 * @param workflowInstanceId
	 * @param eventType
	 * @param eventStatus
	 * @param exception
	 */
	void sendStatusMessage(String workflowInstanceId,EventTypeEnum eventType,EventStatusEnum eventStatus, String exception);
	
}
