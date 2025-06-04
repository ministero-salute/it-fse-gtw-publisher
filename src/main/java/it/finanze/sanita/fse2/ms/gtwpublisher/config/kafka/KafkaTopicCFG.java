
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.ProfileUtility;
import jakarta.annotation.PostConstruct;
import lombok.Data;

/**
 *
 * Kafka topic configuration.
 */
@Data
@Component
public class KafkaTopicCFG {

    @Autowired
    private ProfileUtility profileUtility;

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
    @Value("${kafka.dispatcher-publisher.base-topic}")
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

    @Value("${kafka.udp-publisher.topic}")
    private String selfPublisherTopic;

    @Value("${kafka.udp-publisher.deadletter.topic}")
    private String selfPublisherDeadLetterTopic;

    @PostConstruct
    public void afterInit() {
        if (profileUtility.isTestProfile()) {
            this.indexerPublisherTopic = Constants.Profile.TEST_PREFIX + this.indexerPublisherTopic;
            this.indexerPublisherDeadLetterTopic = Constants.Profile.TEST_PREFIX + this.indexerPublisherDeadLetterTopic;
            this.dispatcherPublisherTopic = Constants.Profile.TEST_PREFIX + this.dispatcherPublisherTopic;
            this.dispatcherPublisherDeadLetterTopic = Constants.Profile.TEST_PREFIX
                    + this.dispatcherPublisherDeadLetterTopic;
            this.statusManagerTopic = Constants.Profile.TEST_PREFIX + this.statusManagerTopic;
            this.selfPublisherTopic = Constants.Profile.TEST_PREFIX + this.selfPublisherTopic;
            this.selfPublisherDeadLetterTopic = Constants.Profile.TEST_PREFIX + this.selfPublisherDeadLetterTopic;
        }
    }
}
