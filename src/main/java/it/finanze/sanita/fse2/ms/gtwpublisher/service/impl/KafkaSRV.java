package it.finanze.sanita.fse2.ms.gtwpublisher.service.impl;

import java.util.Date;

import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaConsumerPropertiesCFG;
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

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.KafkaStatusManagerDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ErrorLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventSourceEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.logging.ElasticLoggerHelper;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.ProfileUtility;
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
	private transient KafkaTemplate<String, String> txKafkaTemplate;

	/**
	 * Not transactional producer.
	 */
	@Autowired
	@Qualifier("notxkafkatemplate")
	private transient KafkaTemplate<String, String> notxKafkaTemplate;

	@Autowired
	private IEdsClient edsClient;
	
	@Autowired
	private transient KafkaTopicCFG kafkaTopicCFG;

	@Autowired
	private transient ElasticLoggerHelper elasticLogger;

	@Autowired
	private transient ProfileUtility profileUtility;

	@Autowired
	private transient KafkaConsumerPropertiesCFG kafkaConsumerPropertiesCFG;

	@Override
	public RecordMetadata sendMessage(String topic, String key, String value, boolean trans) {
		RecordMetadata out = null;
		ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, value);
		try { 
			out = kafkaSend(producerRecord, trans);
		} catch (Exception e) {
			log.error("Send failed.", e); 
			throw new BusinessException(e);
		}   
		return out;
	}

	@Override
	@KafkaListener(topics = "#{'${kafka.indexer-publisher.topic.low-priority}'}", clientIdPrefix = "#{'${kafka.consumer.indexer.client-id-priority.low}'}", containerFactory = "kafkaIndexerListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-indexer}'}")
	public void lowPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders) {
		this.abstractListener(cr, EventSourceEnum.INDEXER, PriorityTypeEnum.LOW);
	}

	@Override
	@KafkaListener(topics = "#{'${kafka.indexer-publisher.topic.medium-priority}'}", clientIdPrefix = "#{'${kafka.consumer.indexer.client-id-priority.medium}'}", containerFactory = "kafkaIndexerListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-indexer}'}")
	public void mediumPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders) {
		this.abstractListener(cr, EventSourceEnum.INDEXER, PriorityTypeEnum.MEDIUM);
	}

	@Override
	@KafkaListener(topics = "#{'${kafka.indexer-publisher.topic.high-priority}'}", clientIdPrefix = "#{'${kafka.consumer.indexer.client-id-priority.high}'}", containerFactory = "kafkaIndexerListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-indexer}'}")
	public void highPriorityListenerIndexer(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders) {
		this.abstractListener(cr, EventSourceEnum.INDEXER, PriorityTypeEnum.HIGH);
	}

	@Override
	@KafkaListener(topics = "#{'${kafka.dispatcher-publisher.topic.low-priority}'}", clientIdPrefix = "#{'${kafka.consumer.dispatcher.client-id-priority.low}'}", containerFactory = "kafkaDispatcherListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-dispatcher}'}")
	public void lowPriorityListenerDispatcher(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders) {
		this.abstractListener(cr, EventSourceEnum.DISPATCHER, PriorityTypeEnum.LOW);
	}

	@Override
	@KafkaListener(topics = "#{'${kafka.dispatcher-publisher.topic.medium-priority}'}", clientIdPrefix = "#{'${kafka.consumer.dispatcher.client-id-priority.medium}'}", containerFactory = "kafkaDispatcherListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-dispatcher}'}")
	public void mediumPriorityListenerDispatcher(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders) {
		this.abstractListener(cr, EventSourceEnum.DISPATCHER, PriorityTypeEnum.MEDIUM);
	}

	@Override
	@KafkaListener(topics = "#{'${kafka.dispatcher-publisher.topic.high-priority}'}", clientIdPrefix = "#{'${kafka.consumer.dispatcher.client-id-priority.high}'}", containerFactory = "kafkaDispatcherListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id-dispatcher}'}")
	public void highPriorityListenerDispatcher(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders) {
		this.abstractListener(cr, EventSourceEnum.DISPATCHER, PriorityTypeEnum.HIGH);
	}

	@SuppressWarnings("unchecked")
	private RecordMetadata kafkaSend(ProducerRecord<String, String> producerRecord, boolean trans) {
		RecordMetadata out = null;
		Object result = null;

		if (trans) {  
			result = txKafkaTemplate.executeInTransaction(t -> { 
				try {
					return t.send(producerRecord).get();
				} catch(InterruptedException e) {
					log.error("InterruptedException caught. Interrupting thread...");					
					Thread.currentThread().interrupt(); 
					throw new BusinessException(e); 
				}
				catch (Exception e) {
					throw new BusinessException(e);
				}  
			});  
		} else { 
			notxKafkaTemplate.send(producerRecord);
		} 

		if(result != null) {
			SendResult<String,String> sendResult = (SendResult<String,String>) result;
			out = sendResult.getRecordMetadata();
			log.info("Send success.");
		}

		return out;
	}

	private void abstractListener(final ConsumerRecord<String, String> cr, EventSourceEnum eventSource, PriorityTypeEnum priorityType) {
		log.info("Listening from: {} with {} priority", eventSource.getName(), priorityType.getCode());
		Date startDateOperation = new Date();

		log.info("Consuming Transaction Event - Message received from topic {} with key {}", cr.topic(), cr.key());
		IndexerValueDTO valueInfo = new Gson().fromJson(cr.value(), IndexerValueDTO.class);

		EventTypeEnum eventType = EventTypeEnum.SEND_TO_EDS;

		try {
			if(!StringUtility.isNullOrEmpty(valueInfo.getWorkflowInstanceId())) {

				EdsPublicationResponseDTO response = new EdsPublicationResponseDTO();
				if (StringUtility.isNullOrEmpty(valueInfo.getIdentificativoDocUpdate())) {
					response = edsClient.sendData(valueInfo.getWorkflowInstanceId());
				} else {
					response = edsClient.sendUpdateData(valueInfo);
				}

				if ((response != null && Boolean.TRUE.equals(response.getEsito())) || profileUtility.isTestProfile() || profileUtility.isDevProfile()) {
					elasticLogger.info("Successfully sent data to EDS for workflow instance id" + valueInfo.getWorkflowInstanceId(), OperationLogEnum.SEND_EDS, ResultLogEnum.OK, startDateOperation);
					sendStatusMessage(valueInfo.getWorkflowInstanceId(), eventType , EventStatusEnum.SUCCESS, null);
				}
			} else {
				String consumedEvent = "";
				switch (eventSource) {
					case INDEXER:
						consumedEvent = "Indexer";
						break;
					case DISPATCHER:
						consumedEvent = "TSFeeding";
						break;
					default:
						consumedEvent = "undefined";
						break;
				}
				log.warn("Error consuming {} Event with key {}: null received", consumedEvent, cr.key());
				elasticLogger.error("Error consuming Kafka Event with key " + cr.key() + ": null received", OperationLogEnum.SEND_EDS, ResultLogEnum.KO, startDateOperation, ErrorLogEnum.KO_EDS);
				sendStatusMessage(valueInfo.getWorkflowInstanceId(), eventType , EventStatusEnum.BLOCKING_ERROR, null);
			}
		} catch (Exception e) {
			elasticLogger.error("Error sending data to EDS", OperationLogEnum.SEND_EDS, ResultLogEnum.KO, startDateOperation, ErrorLogEnum.KO_EDS);
			deadLetterHelper(e);
			if (!kafkaConsumerPropertiesCFG.getDeadLetterExceptions().contains(e.getClass().getName())) {
				sendStatusMessage(valueInfo.getWorkflowInstanceId(), eventType, EventStatusEnum.NON_BLOCKING_ERROR, ExceptionUtils.getStackTrace(e));
			} else {
				sendStatusMessage(valueInfo.getWorkflowInstanceId(), eventType, EventStatusEnum.BLOCKING_ERROR, ExceptionUtils.getStackTrace(e));
			}
			throw e;
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
					exception(exception).
					build();
			String json = StringUtility.toJSONJackson(statusManagerMessage);
			sendMessage(kafkaTopicCFG.getStatusManagerTopic(), workflowInstanceId, json, true);
		} catch(Exception ex) {
			log.error("Error while send status message on indexer : " , ex);
			throw new BusinessException(ex);
		}
	}
}
