package it.finanze.sanita.fse2.ms.gtwpublisher.service.impl;

import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaPropertiesCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.KafkaStatusManagerDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ErrorLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.logging.ElasticLoggerHelper;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.EncryptDecryptUtility;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author vincenzoingenito
 *
 * Kafka management service.
 */
@Service
@Slf4j
public class KafkaSRV implements IKafkaSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 987723954716001270L;


	/**
	 * Transactional producer.
	 */
	@Autowired
	@Qualifier("txkafkatemplate")
	private KafkaTemplate<String, String> txKafkaTemplate;

	/**
	 * Not transactional producer.
	 */
	@Autowired
	@Qualifier("notxkafkatemplate")
	private KafkaTemplate<String, String> notxKafkaTemplate;

	@Autowired
	private KafkaPropertiesCFG kafkaPropCFG;

	@Autowired
	private IEdsInvocationSRV edsInvocationSRV;
	
	@Autowired
	private KafkaTopicCFG kafkaTopicCFG;

	@Autowired
	private ElasticLoggerHelper elasticLogger;
	
	@Override
	public RecordMetadata sendMessage(String topic, String key, String value, boolean trans) {
		RecordMetadata out = null;
		ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value); 
		try { 
			out = kafkaSend(record, trans);
		} catch (Exception e) {
			log.error("Send failed.", e); 
			throw new BusinessException(e);
		}   
		return out;
	} 

	private RecordMetadata kafkaSend(ProducerRecord<String, String> record, boolean trans) {
		RecordMetadata out = null;
		Object result = null;

		if (trans) {  
			result = txKafkaTemplate.executeInTransaction(t -> { 
				try {
					return t.send(record).get();
				} catch (Exception e) {
					throw new BusinessException(e);
				}  
			});  
		} else { 
			notxKafkaTemplate.send(record);
		} 

		if(result != null) {
			SendResult<String,String> sendResult = (SendResult<String,String>) result;
			out = sendResult.getRecordMetadata();
			log.info("Send success.");
		}

		return out;
	}

	@Override
	@KafkaListener(topics = "#{'${kafka.indexer-publisher.topic}'}",  clientIdPrefix = "#{'${kafka.consumer.client-id-indexer}'}", containerFactory = "kafkaIndexerListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-indexer}'}")
	public void listenerIndexer(final ConsumerRecord<String, String> cr, final MessageHeaders messageHeaders) {

		Date startDateOperation = new Date();

		String message = cr.value();
		log.info("Consuming Transaction Event - Message received with key {}", cr.key());
		String workflowInstanceId = "";

		EventTypeEnum eventType = null;
		try {
			workflowInstanceId = EncryptDecryptUtility.decryptObject(kafkaPropCFG.getCrypto(), message, String.class);

			if(!StringUtility.isNullOrEmpty(workflowInstanceId)) {
				log.info("WORKFLOW INSTANCE ID FROM INDEXER: " + workflowInstanceId);
				Boolean sendToEdsCompleted = edsInvocationSRV.findAndSendToEdsByWorkflowInstanceId(workflowInstanceId);
				eventType = EventTypeEnum.SEND_TO_EDS;
				if(Boolean.TRUE.equals(sendToEdsCompleted)) {
					sendStatusMessage(workflowInstanceId, eventType , EventStatusEnum.SUCCESS,null);
				}
			} else {
				log.warn("Error consuming Kafka Event with key {}: null received", cr.key());
				elasticLogger.error("Error consuming Kafka Event with key " + cr.key() + ": null received", OperationLogEnum.SEND_EDS, ResultLogEnum.KO, startDateOperation, ErrorLogEnum.KO_EDS);

			}

			elasticLogger.info("Successfully sent data to EDS for workflow instance id " + workflowInstanceId, OperationLogEnum.SEND_EDS, ResultLogEnum.OK, startDateOperation);

		} catch (Exception e) {
			if(eventType == null) {
				eventType = EventTypeEnum.GENERIC_ERROR;
			}

			elasticLogger.error("Error sending data to EDS", OperationLogEnum.SEND_EDS, ResultLogEnum.KO, startDateOperation, ErrorLogEnum.KO_EDS);

			sendStatusMessage(workflowInstanceId, eventType, EventStatusEnum.ERROR,ExceptionUtils.getStackTrace(e));
			deadLetterHelper(e);
			throw new BusinessException(e);
		}
	}



	@Override
	@KafkaListener(topics = "#{'${kafka.dispatcher-publisher.topic}'}",  clientIdPrefix = "#{'${kafka.consumer.client-id-dispatcher}'}", containerFactory = "kafkaDispatcherListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-dispatcher}'}")
	public void listenerDispatcher(final ConsumerRecord<String, String> cr, final MessageHeaders messageHeaders) {

		Date startDateOperation = new Date();

		String message = cr.value();
		log.info("Consuming Transaction Event - Message received with key {}", cr.key());
		String workflowInstanceId = "";

		EventTypeEnum eventType = null;
		try {
			workflowInstanceId = EncryptDecryptUtility.decryptObject(kafkaPropCFG.getCrypto(), message, String.class);

			if(!StringUtility.isNullOrEmpty(workflowInstanceId)) {
				log.info("WORKFLOW INSTANCE ID FROM DISPATCHER: " + workflowInstanceId);
				Boolean sendToEdsCompleted = edsInvocationSRV.findAndSendToEdsByWorkflowInstanceId(workflowInstanceId);
				eventType = EventTypeEnum.SEND_TO_EDS;
				if(Boolean.TRUE.equals(sendToEdsCompleted)) {
					sendStatusMessage(workflowInstanceId, eventType , EventStatusEnum.SUCCESS,null);
				}
			} else {
				log.warn("Error consuming Validation Event with key {}: null received", cr.key());
			}

			elasticLogger.info("Successfully sent data to EDS for workflow instance id " + workflowInstanceId, OperationLogEnum.SEND_EDS, ResultLogEnum.OK, startDateOperation);


		} catch (Exception e) {
			if(eventType == null) {
				eventType = EventTypeEnum.GENERIC_ERROR;
			}

			elasticLogger.error("Error sending data to EDS", OperationLogEnum.SEND_EDS, ResultLogEnum.KO, startDateOperation, ErrorLogEnum.KO_EDS);

			sendStatusMessage(workflowInstanceId, eventType, EventStatusEnum.ERROR,ExceptionUtils.getStackTrace(e));
			deadLetterHelper(e);
			throw new BusinessException(e);
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

		log.error("{}", sb.toString());
	}

	@Override
	public void sendStatusMessage(final String workflowInstanceId,final EventTypeEnum eventType,
			final EventStatusEnum eventStatus, String exception) {
		try {
			KafkaStatusManagerDTO statusManagerMessage = KafkaStatusManagerDTO.builder().
					eventType(eventType).
					eventDate(new Date()).
					eventStatus(eventStatus).
					exception(exception).
					build();
			String json = StringUtility.toJSONJackson(statusManagerMessage);
			String cryptoMessage = EncryptDecryptUtility.encryptObject(kafkaPropCFG.getCrypto(), json);
			sendMessage(kafkaTopicCFG.getStatusManagerTopic(), workflowInstanceId, cryptoMessage, true);
		} catch(Exception ex) {
			log.error("Error while send status message on indexer : " , ex);
			throw new BusinessException(ex);
		}
	}

}
