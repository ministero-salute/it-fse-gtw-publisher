/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 *	Kafka consumer properties configuration.
 */
@Data
@Component
@Slf4j
public class KafkaConsumerPropertiesCFG implements Serializable {

	public static final int MAX_ATTEMPT = 5;
    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 4863316988401046567L;

	/**
	 * Client id indexer.
	 */
	@Value("${kafka.consumer.client-id-indexer}")
	private String clientIdIndexer;

	/**
	 * Group id consumer indexer.
	 */
	@Value("${kafka.consumer.group-id-indexer}")
	private String consumerGroupIdIndexer;


	/**
	 * Client id dispatcher.
	 */
	@Value("${kafka.consumer.client-id-dispatcher}")
	private String clientIdDispatcher;

	/**
	 * Group id consumer dispather.
	 */
	@Value("${kafka.consumer.group-id-dispatcher}")
	private String consumerGroupIdDispatcher;

	/**
	 * Consumer key deserializer.
	 */
	@Value("${kafka.consumer.key-deserializer}")
	private String consumerKeyDeserializer;

	/**
	 * Consumer value deserializer.
	 */
	@Value("${kafka.consumer.value-deserializer}")
	private String consumerValueDeserializer;

	/**
	 * Consumer bootstrap server.
	 */
	@Value("${kafka.consumer.bootstrap-servers}")
	private String consumerBootstrapServers;

	/**
	 * Isolation level.
	 */
	@Value("${kafka.consumer.isolation.level}")
	private String isolationLevel;

	/**
	 * Flag autocommit.
	 */
	@Value("${kafka.consumer.auto-commit}")
	private String autoCommit;

	/**
	 * Flag auto offset reset.
	 */
	@Value("${kafka.consumer.auto-offset-reset}")
	private String autoOffsetReset;

	/**
	 * Eccezioni per dead letter.
	 */
	@Value("#{${kafka.consumer.dead-letter-exc}}")
	private List<String> deadLetterExceptions;

	/**
	 * Flag enable ssl.
	 */
	@Value("${kafka.enablessl}")
	private boolean enableSsl;
	
	/**
	 * Kafka retry.
	 */
	@Value("${kafka.retry}")
	private Integer nRetry;
	
	/**
	 * Eccezioni temporanee.
	 */
	@Value("#{${kafka.consumer.temporary-exc}}")
	private List<String> temporaryExceptions;

	public Optional<EventStatusEnum> asExceptionType(Exception e) {
		// Retrieve exception identifier
		EventStatusEnum status = null;

		String identifier = e.getClass().getCanonicalName();
		// Identify
		if(deadLetterExceptions.contains(identifier)) status = EventStatusEnum.BLOCKING_ERROR;
		if(temporaryExceptions.contains(identifier)) status = EventStatusEnum.NON_BLOCKING_ERROR;
		// Return
		return Optional.ofNullable(status);
	}

	public void deadLetterHelper(Exception e) {
		StringBuilder sb = new StringBuilder("LIST OF USEFUL EXCEPTIONS TO MOVE TO DEADLETTER OFFSET 'kafka.consumer.dead-letter-exc'. ");
		boolean continua = true;
		Throwable excTmp = e;
		Throwable excNext = null;

		while (continua) {
			if (excNext != null) {
				excTmp = excNext;
				sb.append(", ");
			}

			sb.append(excTmp.getClass().getCanonicalName());
			excNext = excTmp.getCause();

			if (excNext == null) {
				continua = false;
			}

		}

		log.error("{}", sb);
	}

}
