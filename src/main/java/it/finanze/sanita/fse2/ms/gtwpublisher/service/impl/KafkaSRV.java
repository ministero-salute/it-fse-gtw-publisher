/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.service.impl;

import com.google.gson.Gson;
import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.client.base.ClientCallback;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.AccreditationSimulationCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaConsumerPropertiesCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.KafkaStatusManagerDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BlockingEdsException;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IAccreditamentoSimulationSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.KafkaAbstractSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum.*;
import static it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum.DESERIALIZE;
import static it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum.SEND_TO_EDS;
import static it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum.*;
import static it.finanze.sanita.fse2.ms.gtwpublisher.enums.ProcessorOperationEnum.PUBLISH;

/**
 * Kafka management service.
 */
@Service
@Slf4j
public class KafkaSRV extends KafkaAbstractSRV implements IKafkaSRV {

	@Autowired
	private IEdsClient edsClient;

	@Autowired
	private KafkaTopicCFG topicCFG;

	@Autowired
	private KafkaConsumerPropertiesCFG kafkaConsumerPropCFG;
	
	@Autowired
	private IAccreditamentoSimulationSRV accreditamentoSimSRV;
	
	@Autowired
	private AccreditationSimulationCFG accreditamentoSimulationCFG;
	
	@Value("${spring.application.name}")
	private String msName;
	

	@Override
	@KafkaListener(topics = "#{'${kafka.indexer-publisher.topic.low-priority}'}", clientIdPrefix = "#{'${kafka.consumer.indexer.client-id-priority.low}'}", containerFactory = "kafkaIndexerListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-indexer}'}")
	public void lowPriorityListenerIndexer(ConsumerRecord<String, String> cr, @Header(KafkaHeaders.DELIVERY_ATTEMPT) int delivery) throws Exception {
		log.debug("Listening with {} priority", LOW.getDescription());
		loop(cr, IndexerValueDTO.class, (req) ->  publishAndReplace(req, LOW), delivery);
	}

	@Override
	@KafkaListener(topics = "#{'${kafka.indexer-publisher.topic.medium-priority}'}", clientIdPrefix = "#{'${kafka.consumer.indexer.client-id-priority.medium}'}", containerFactory = "kafkaIndexerListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-indexer}'}")
	public void mediumPriorityListenerIndexer(ConsumerRecord<String, String> cr, @Header(KafkaHeaders.DELIVERY_ATTEMPT) int delivery) throws Exception {
		log.debug("Listening with {} priority", MEDIUM.getDescription());
		loop(cr, IndexerValueDTO.class, (req) ->  publishAndReplace(req, MEDIUM), delivery);
	}

	@Override
	@KafkaListener(topics = "#{'${kafka.indexer-publisher.topic.high-priority}'}", clientIdPrefix = "#{'${kafka.consumer.indexer.client-id-priority.high}'}", containerFactory = "kafkaIndexerListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-indexer}'}")
	public void highPriorityListenerIndexer(ConsumerRecord<String, String> cr, @Header(KafkaHeaders.DELIVERY_ATTEMPT) int delivery) throws Exception {
		log.debug("Listening with {} priority", HIGH.getDescription());
		loop(cr, IndexerValueDTO.class, (req) ->  publishAndReplace(req, HIGH), delivery);
	}
	
	private EdsTraceResponseDTO publishAndReplace(IndexerValueDTO dto, PriorityTypeEnum priority) {

		if(accreditamentoSimulationCFG.isEnableCheck()) accreditamentoSimSRV.runSimulation(dto.getIdDoc());

		EdsTraceResponseDTO response;
		if (dto.getEdsDPOperation().equals(PUBLISH)) {
			response = edsClient.sendPublicationData(dto, priority);
		} else {
			response = edsClient.sendReplaceData(dto);
		}

		return response;
	}

	private <T> void loop(ConsumerRecord<String, String> cr, Class<T> clazz, ClientCallback<T, EdsTraceResponseDTO> cb, int delivery) throws Exception {

		// ====================
		// Deserialize request
		// ====================
		// Retrieve request body
		String wif = cr.key();
		String request = cr.value();
		T req;
		boolean exit = false;
		// Convert request
		try {
			// Get object
			req = new Gson().fromJson(request, clazz);
			// Require not null
			Objects.requireNonNull(req, "The request payload cannot be null");
		} catch (Exception e) {
			log.error("Unable to deserialize request with wif {} due to: {}", wif, e.getMessage());
			sendStatusMessage(wif, DESERIALIZE, BLOCKING_ERROR, request);
			throw new BlockingEdsException(e.getMessage());
		}

		// ====================
		// Retry iterations
		// ====================
		Exception ex = new Exception("Errore generico durante l'invocazione del client di eds");
		// Iterate
		for (int i = 0; i <= kafkaConsumerPropCFG.getNRetry() && !exit; ++i) {
			try {
				// Execute request
				EdsTraceResponseDTO res = cb.request(req);
				// Everything has been resolved
				if (Boolean.TRUE.equals(res.getEsito())) {
					sendStatusMessage(wif, SEND_TO_EDS, SUCCESS, new Gson().toJson(res));
				} else {
					throw new BlockingEdsException(res.getMessageError());
				}
				// Quit flag
				exit = true;
			} catch (Exception e) {
				// Assign
				ex = e;
				// Display help
				kafkaConsumerPropCFG.deadLetterHelper(e);
				// Try to identify the exception type
				Optional<EventStatusEnum> type = kafkaConsumerPropCFG.asExceptionType(e);
				// If we found it, we are good to make an action, otherwise, let's retry
				if(type.isPresent()) {
					// Get type [BLOCKING or NON_BLOCKING_ERROR]
					EventStatusEnum status = type.get();
					// Send to kafka
					if (delivery <= KafkaConsumerPropertiesCFG.MAX_ATTEMPT) {
						// Send to kafka
						sendStatusMessage(wif, SEND_TO_EDS, status, e.getMessage());
					}
					// We are going re-process it
					throw e;
				}
			}
		}

		// We didn't exit properly from the loop,
		// We reached the max amount of retries
		if(!exit) {
			sendStatusMessage(wif, SEND_TO_EDS, BLOCKING_ERROR_MAX_RETRY, "Massimo numero di retry raggiunto: " + ex.getMessage());
			throw new BlockingEdsException(ex.getMessage());
		}

	}

	@Override
	public void sendStatusMessage(final String workflowInstanceId,final EventTypeEnum eventType,
			final EventStatusEnum eventStatus, String exception) {
		try {
			KafkaStatusManagerDTO statusManagerMessage = KafkaStatusManagerDTO.builder().
					eventType(eventType).
					eventDate(new Date()).
					eventStatus(eventStatus).
					message(exception).
					microserviceName(msName).
					build();
			String json = StringUtility.toJSONJackson(statusManagerMessage);
			sendMessage(topicCFG.getStatusManagerTopic(), workflowInstanceId, json, true);
		} catch(Exception ex) {
			log.error("Error while send status message on indexer : " , ex);
			throw new BusinessException(ex);
		}
	}
}
