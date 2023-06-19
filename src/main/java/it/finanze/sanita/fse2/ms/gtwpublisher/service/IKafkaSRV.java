/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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
