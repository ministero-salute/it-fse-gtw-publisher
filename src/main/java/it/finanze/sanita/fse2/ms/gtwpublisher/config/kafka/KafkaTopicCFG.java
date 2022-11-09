/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
