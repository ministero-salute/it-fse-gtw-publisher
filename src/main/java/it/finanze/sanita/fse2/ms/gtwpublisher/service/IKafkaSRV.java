/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.messaging.MessageHeaders;

import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;
 

public interface IKafkaSRV {

	/**
	 * Send message over kafka topic
	 * @param topic
	 * @param key
	 * @param value
	 * @param trans
	 * @return
	 */
	RecordMetadata sendMessage(String topic, String key, String value, boolean trans) ;
 
	/**
	 * Kafka listener for Indexer communications in low priority
	 * @param cr
	 * @param messageHeaders
	 */
	void lowPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders) throws Exception;

	/**
	 * Kafka listener for Indexer communications in medium priority
	 * @param cr
	 * @param messageHeaders
	 */
	void mediumPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders) throws Exception;

	/**
	 * Kafka listener for Indexer communications in high priority
	 * @param cr
	 * @param messageHeaders
	 */
	void highPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders) throws Exception;

	/**
	 * Send status message to respective topic
	 * @param workflowInstanceId
	 * @param eventType
	 * @param eventStatus
	 * @param exception
	 */
	void sendStatusMessage(String workflowInstanceId,EventTypeEnum eventType,EventStatusEnum eventStatus, String exception);
	
}
