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
	 * Kafka listener for Indexer communications
	 * @param cr
	 * @param messageHeaders
	 */
	void listenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders);

	/**
	 * Kafka listener for Dispatcher communications
	 * @param cr
	 * @param messageHeaders
	 */
	void listenerDispatcher(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders);
	
	void sendStatusMessage(String transactionId,EventTypeEnum eventType,EventStatusEnum eventStatus, String exception);
	
}
