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

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaProducerPropertiesCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public abstract class KafkaAbstractSRV {
	/**
	 * Transactional producer.
	 */
	@Autowired
	@Qualifier("txkafkatemplate")
	protected KafkaTemplate<String, String> txKafkaTemplate;

	/**
	 * Not transactional producer.
	 */
	@Autowired
	@Qualifier("notxkafkatemplate")
	protected KafkaTemplate<String, String> notxKafkaTemplate;

	@Autowired
	protected KafkaTopicCFG kafkaTopicCFG;
	
	@Autowired
	private KafkaProducerPropertiesCFG kafkaProducerCFG;

	public RecordMetadata sendMessage(String topic, String key, String value) {
		RecordMetadata out = null;
		ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, value);
		try {
			out = kafkaSend(producerRecord);
		} catch (Exception e) {
			log.error("Send failed.", e);
			throw new BusinessException(e);
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	protected RecordMetadata kafkaSend(ProducerRecord<String, String> producerRecord) {
		RecordMetadata out = null;
		Object result = null;

		boolean trans = !StringUtility.isNullOrEmpty(kafkaProducerCFG.getTransactionalId());
		
		if (trans) {
			result = txKafkaTemplate.executeInTransaction(t -> {
				try {
					return t.send(producerRecord).get();
				} catch (InterruptedException e) {
					log.error("InterruptedException caught. Interrupting thread...");
					Thread.currentThread().interrupt();
					throw new BusinessException(e);
				} catch (Exception e) {
					throw new BusinessException(e);
				}
			});
		} else {
			notxKafkaTemplate.send(producerRecord);
		}

		if (result != null) {
			SendResult<String, String> sendResult = (SendResult<String, String>) result;
			out = sendResult.getRecordMetadata();
			log.debug("Message sent successfully");
		}

		return out;
	}
}
