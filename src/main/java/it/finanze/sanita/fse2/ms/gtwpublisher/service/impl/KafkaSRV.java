/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.service.impl;

import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.AccreditationSimulationCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaConsumerPropertiesCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.KafkaStatusManagerDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BlockingEdsException;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IAccreditamentoSimulationSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.KafkaAbstractSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

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
	private KafkaConsumerPropertiesCFG kafkaConsumerPropertiesCFG;
	
	@Autowired
	private IAccreditamentoSimulationSRV accreditamentoSimSRV;
	
	@Autowired
	private AccreditationSimulationCFG accreditamentoSimulationCFG;
	
	@Value("${spring.application.name}")
	private String msName;
	

	@Override
	@KafkaListener(topics = "#{'${kafka.indexer-publisher.topic.low-priority}'}", clientIdPrefix = "#{'${kafka.consumer.indexer.client-id-priority.low}'}", containerFactory = "kafkaIndexerListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-indexer}'}")
	public void lowPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders) throws Exception {
		genericListener(cr, PriorityTypeEnum.LOW);
	}

	@Override
	@KafkaListener(topics = "#{'${kafka.indexer-publisher.topic.medium-priority}'}", clientIdPrefix = "#{'${kafka.consumer.indexer.client-id-priority.medium}'}", containerFactory = "kafkaIndexerListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-indexer}'}")
	public void mediumPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders) throws Exception {
		genericListener(cr, PriorityTypeEnum.MEDIUM);
	}

	@Override
	@KafkaListener(topics = "#{'${kafka.indexer-publisher.topic.high-priority}'}", clientIdPrefix = "#{'${kafka.consumer.indexer.client-id-priority.high}'}", containerFactory = "kafkaIndexerListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-indexer}'}")
	public void highPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders)throws Exception  {
		genericListener(cr, PriorityTypeEnum.HIGH);
	}
	

	private void genericListener(final ConsumerRecord<String, String> cr, PriorityTypeEnum priorityType) throws Exception {
		log.debug("Listening with {} priority", priorityType.getDescription());
		Date startDateOperation = new Date();

		log.debug("Consuming Transaction Event - Message received from topic {} with key {}", cr.topic(), cr.key());
		IndexerValueDTO valueInfo = new Gson().fromJson(cr.value(), IndexerValueDTO.class);

		EventTypeEnum eventType = EventTypeEnum.SEND_TO_EDS;

		boolean esito = false;
		int counter = 0;

		EdsPublicationResponseDTO response = null;
		while(Boolean.FALSE.equals(esito) && counter<=kafkaConsumerPropertiesCFG.getNRetry()) {
			try {
				if(!StringUtility.isNullOrEmpty(valueInfo.getWorkflowInstanceId())) {
					
					if(accreditamentoSimulationCFG.isEnableCheck()) {
						accreditamentoSimSRV.runSimulation(valueInfo.getIdDoc());
					}
					
					if (valueInfo.getEdsDPOperation().equals(ProcessorOperationEnum.PUBLISH)) {
						response = edsClient.sendPublicationData(valueInfo, priorityType);
					} else {
						response = edsClient.sendReplaceData(valueInfo);
					}

					if (Boolean.TRUE.equals(response.getEsito())) {
						esito = response.getEsito();
						log.debug("Successfully sent data to EDS for workflow instance id" + valueInfo.getWorkflowInstanceId(), OperationLogEnum.SEND_EDS, ResultLogEnum.OK, startDateOperation);
						sendStatusMessage(valueInfo.getWorkflowInstanceId(), eventType , EventStatusEnum.SUCCESS, null);
					} else {
						try {
							Class<? extends Exception> s = (Class<? extends Exception>) Class.forName(response.getExClassCanonicalName());
							throw s.newInstance();
						} catch(IllegalAccessException | InstantiationException | ClassNotFoundException e) {
							throw new BusinessException(e);
						} 
					}
				} else {
					log.error("Error consuming Kafka Event with key " + cr.key() + ": null received", OperationLogEnum.SEND_EDS, ResultLogEnum.KO, startDateOperation);
					sendStatusMessage(valueInfo.getWorkflowInstanceId(), eventType , EventStatusEnum.BLOCKING_ERROR, null);
				}
			} catch (Exception e) {
				String canonicalName = ExceptionUtils.getRootCause(e).getClass().getCanonicalName();
				String errorMessage = "";
				if(response!=null && response.getExClassCanonicalName()!=null) {
					canonicalName = response.getExClassCanonicalName();
					errorMessage = response.getMessageError();
				}
				
				log.error("Error sending data to EDS", OperationLogEnum.SEND_EDS, ResultLogEnum.KO, startDateOperation);
				deadLetterHelper(e);
				
				if(StringUtility.isNullOrEmpty(e.getMessage())) {
					errorMessage = "Errore generico durante l'invocazione del client di eds";	
				}
				if(kafkaConsumerPropertiesCFG.getDeadLetterExceptions().contains(canonicalName)) {
					log.debug("Dead letter Exception : " + canonicalName);
					sendStatusMessage(valueInfo.getWorkflowInstanceId(), eventType, EventStatusEnum.BLOCKING_ERROR, errorMessage);
					throw e;
				} else if (kafkaConsumerPropertiesCFG.getTemporaryExceptions().contains(canonicalName)) {
					log.debug("Temporary Exception : " + canonicalName);
					sendStatusMessage(valueInfo.getWorkflowInstanceId(), eventType, EventStatusEnum.NON_BLOCKING_ERROR, errorMessage);
					throw e;
				} else {
					counter++;
					log.debug("Generic Exception : " + canonicalName + " Retry:" + counter +" di" + kafkaConsumerPropertiesCFG.getNRetry());
					if(counter==kafkaConsumerPropertiesCFG.getNRetry()) {
						sendStatusMessage(valueInfo.getWorkflowInstanceId(), eventType, EventStatusEnum.BLOCKING_ERROR_MAX_RETRY, "Massimo numero di retry raggiunto :" + errorMessage);
						throw new BlockingEdsException("Numero massimo di retry raggiunto" , e);
					}
				}
				
			}
		}
		
		
	}

	/**
	 * @param e
	 */
	private void deadLetterHelper(Exception e) {
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
