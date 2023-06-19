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
package it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka;

import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.ProfileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

import javax.annotation.PostConstruct;

/**
 *
 *	Kafka topic configuration.
 */
@Data
@Component
public class KafkaTopicCFG {

	@Autowired
	private ProfileUtility profileUtility;

	/**
	 * Topic.
	 */
	@Value("${kafka.indexer-publisher.topic.low-priority}")
	private String indexerPublisherLowPriorityTopic;

	/**
	 * Topic.
	 */
	@Value("${kafka.indexer-publisher.topic.medium-priority}")
	private String indexerPublisherMediumPriorityTopic;

	/**
	 * Topic.
	 */
	@Value("${kafka.indexer-publisher.topic.high-priority}")
	private String indexerPublisherHighPriorityTopic;
	
	/**
	 * Dead letter topic.
	 */
	@Value("${kafka.indexer-publisher.deadletter.topic}")
	private String indexerPublisherDeadLetterTopic;


	/**
	 * Topic.
	 */
	@Value("${kafka.dispatcher-publisher.topic.low-priority}")
	private String dispatcherPublisherLowPriorityTopic;

	/**
	 * Topic.
	 */
	@Value("${kafka.dispatcher-publisher.topic.medium-priority}")
	private String dispatcherPublisherMediumPriorityTopic;

	/**
	 * Topic.
	 */
	@Value("${kafka.dispatcher-publisher.topic.high-priority}")
	private String dispatcherPublisherHighPriorityTopic;
	
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


	@PostConstruct
	public void afterInit() {
		if (profileUtility.isTestProfile()) {
			this.dispatcherPublisherLowPriorityTopic = Constants.Profile.TEST_PREFIX + this.dispatcherPublisherLowPriorityTopic;
			this.dispatcherPublisherMediumPriorityTopic = Constants.Profile.TEST_PREFIX + this.dispatcherPublisherMediumPriorityTopic;
			this.dispatcherPublisherHighPriorityTopic = Constants.Profile.TEST_PREFIX + this.dispatcherPublisherHighPriorityTopic;
			this.dispatcherPublisherDeadLetterTopic = Constants.Profile.TEST_PREFIX + this.dispatcherPublisherDeadLetterTopic;
			this.indexerPublisherLowPriorityTopic = Constants.Profile.TEST_PREFIX + this.indexerPublisherLowPriorityTopic;
			this.indexerPublisherMediumPriorityTopic = Constants.Profile.TEST_PREFIX + this.indexerPublisherMediumPriorityTopic;
			this.indexerPublisherHighPriorityTopic = Constants.Profile.TEST_PREFIX + this.indexerPublisherHighPriorityTopic;
			this.statusManagerTopic = Constants.Profile.TEST_PREFIX + this.statusManagerTopic;
		}
	}
}
