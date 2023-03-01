/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.service;

import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;
import org.apache.kafka.clients.consumer.ConsumerRecord;
 

public interface IKafkaSRV {
 
	/**
	 * Kafka listener for Indexer communications in low priority
	 */
	void lowPriorityListenerIndexer(ConsumerRecord<String, String> cr, int delivery) throws Exception;

	/**
	 * Kafka listener for Indexer communications in medium priority
	 */
	void mediumPriorityListenerIndexer(ConsumerRecord<String, String> cr, int delivery) throws Exception;

	/**
	 * Kafka listener for Indexer communications in high priority
	 */
	void highPriorityListenerIndexer(ConsumerRecord<String, String> cr, int delivery) throws Exception;

	/**
	 * Send status message to respective topic
	 */
	void sendStatusMessage(String workflowInstanceId,EventTypeEnum eventType,EventStatusEnum eventStatus, String exception);
	
}
