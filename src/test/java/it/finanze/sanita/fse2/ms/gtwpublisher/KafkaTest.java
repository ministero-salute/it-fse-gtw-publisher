package it.finanze.sanita.fse2.ms.gtwpublisher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.finanze.sanita.fse2.ms.gtwpublisher.utility.JsonUtility;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Description;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.ActiveProfiles;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IKafkaSRV;
import lombok.Data;
import lombok.NoArgsConstructor;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class KafkaTest {
	
	@Autowired
	private IKafkaSRV kafkaSRV;

	@Autowired
	private KafkaTopicCFG kafkaTopicCFG;

    @MockBean
	private IEdsClient edsClient;

	@DisplayName("Producer send")
	void kafkaProducerSendTest() {  
		String key = "1";
		String topic = "transactionEvents"; 
 
	/*****************TOPIC**********************/ 
		String message = "Messaggio numero : " + 1;
		RecordMetadata output = kafkaSRV.sendMessage(topic,key, message, true);
		assertEquals(message.length(), output.serializedValueSize() , "Il value non coincide");
		assertEquals(topic,output.topic(), "Il topic non coincide");
		
    }
	

	@Test
	@Description("Generic error test on indexer listener")
	void kafkaListenerIndexerErrorTest() {

		String value = "{\"workflowInstanceId\":\"wii1\",\"identificativoDocUpdate\":\"id1\"}";
		String topic = "topic";
		Map<String, Object> map = new HashMap<>();
		MessageHeaders headers = new MessageHeaders(map);
	    
		Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

	    records.put(new TopicPartition(topic, 0), new ArrayList<ConsumerRecord<String, String>>());
		ConsumerRecord<String, String> record = new ConsumerRecord<String, String>(topic, 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0, topic, value);
	
	    map.put("test", value);

		BDDMockito.when(edsClient.sendData(Mockito.anyString())).thenThrow(BusinessException.class);
		BDDMockito.when(edsClient.sendUpdateData(Mockito.any(IndexerValueDTO.class))).thenThrow(BusinessException.class);

		assertThrows(BusinessException.class, ()->kafkaSRV.lowPriorityListenerIndexer(record, headers));
		assertThrows(BusinessException.class, ()->kafkaSRV.mediumPriorityListenerIndexer(record, headers));
		assertThrows(BusinessException.class, ()->kafkaSRV.highPriorityListenerIndexer(record, headers));
	}

	@Test
	@Description("Generic error test on dispatcher listener")
	void kafkaListenerDispatcherErrorTest() {

		String topic = "topic";
		String value = "{\"workflowInstanceId\":\"wii1\",\"identificativoDocUpdate\":\"id1\"}";

		Map<String, Object> map = new HashMap<>();
		MessageHeaders headers = new MessageHeaders(map);

		Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

		records.put(new TopicPartition(topic, 0), new ArrayList<>());
		ConsumerRecord<String, String> record = new ConsumerRecord<String, String>(topic, 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0, topic, value);

		map.put("test", value);

		BDDMockito.when(edsClient.sendData(Mockito.anyString())).thenThrow(BusinessException.class);
		BDDMockito.when(edsClient.sendUpdateData(Mockito.any(IndexerValueDTO.class))).thenThrow(BusinessException.class);

		assertThrows(BusinessException.class, ()->kafkaSRV.lowPriorityListenerDispatcher(record, headers));
		assertThrows(BusinessException.class, ()->kafkaSRV.mediumPriorityListenerDispatcher(record, headers));
		assertThrows(BusinessException.class, ()->kafkaSRV.highPriorityListenerDispatcher(record, headers));
	}

	@Test
	@Description("Success test on indexer listener")
	void kafkaListenerIndexerSuccessTest() {
		String topicLow = kafkaTopicCFG.getIndexerPublisherLowPriorityTopic();
		String topicMedium = kafkaTopicCFG.getIndexerPublisherMediumPriorityTopic();
		String topicHigh = kafkaTopicCFG.getIndexerPublisherHighPriorityTopic();

		Map<String, Object> map = new HashMap<>();
		MessageHeaders headers = new MessageHeaders(map);

		Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

		records.put(new TopicPartition(topicLow, 0), new ArrayList<>());
		records.put(new TopicPartition(topicMedium, 0), new ArrayList<>());
		records.put(new TopicPartition(topicHigh, 0), new ArrayList<>());
		
		final String value = new Gson().toJson(new IndexerValueDTO(TestConstants.testWorkflowInstanceId, null));

		ConsumerRecord<String, String> recordLow = new ConsumerRecord<>(topicLow, 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0, topicLow, value);
		ConsumerRecord<String, String> recordMedium = new ConsumerRecord<>(topicMedium, 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0, topicMedium, value);
		ConsumerRecord<String, String> recordHigh = new ConsumerRecord<>(topicHigh, 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0, topicHigh, value);

        BDDMockito.given(edsClient.sendData(Mockito.anyString())).willReturn(new EdsPublicationResponseDTO(true, ""));

		assertDoesNotThrow(()->kafkaSRV.lowPriorityListenerIndexer(recordLow, headers));
		assertDoesNotThrow(()->kafkaSRV.mediumPriorityListenerIndexer(recordMedium, headers));
		assertDoesNotThrow(()->kafkaSRV.highPriorityListenerIndexer(recordHigh, headers));
	}

	@Test
	@Description("Success test on dispatcher listener")
	void kafkaListenerDispatcherSuccessTest() {
		String topicLow = kafkaTopicCFG.getDispatcherPublisherLowPriorityTopic();
		String topicMedium = kafkaTopicCFG.getDispatcherPublisherMediumPriorityTopic();
		String topicHigh = kafkaTopicCFG.getDispatcherPublisherHighPriorityTopic();

		Map<String, Object> map = new HashMap<>();
		MessageHeaders headers = new MessageHeaders(map);

		Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

		records.put(new TopicPartition(topicLow, 0), new ArrayList<>());
		records.put(new TopicPartition(topicMedium, 0), new ArrayList<>());
		records.put(new TopicPartition(topicHigh, 0), new ArrayList<>());
		
		final String value = new Gson().toJson(new IndexerValueDTO(TestConstants.testWorkflowInstanceId, null));

		ConsumerRecord<String, String> recordLow = new ConsumerRecord<>(topicLow, 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0, topicLow, value);
		ConsumerRecord<String, String> recordMedium = new ConsumerRecord<>(topicMedium, 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0, topicMedium, value);
		ConsumerRecord<String, String> recordHigh = new ConsumerRecord<>(topicHigh, 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0, topicHigh, value);

		BDDMockito.given(edsClient.sendData(Mockito.anyString())).willReturn(new EdsPublicationResponseDTO(true, ""));

		assertDoesNotThrow(()->kafkaSRV.lowPriorityListenerDispatcher(recordLow, headers));
		assertDoesNotThrow(()->kafkaSRV.mediumPriorityListenerDispatcher(recordMedium, headers));
		assertDoesNotThrow(()->kafkaSRV.highPriorityListenerDispatcher(recordHigh, headers));
	}
 
	@Data
	@NoArgsConstructor
	class KafkaMessageDTO{
		String message;
		
	}


    @Test
	@Description("Send data to gtw-ini-client")
    @Disabled
	void testSendDataToClientMicroservice() {
        // To run this test change the edsClient @MockBean to @Autowired at the top of this class
        // paste a workflowInstanceId present in your ini_eds_invocation mongo collection

        String workFlowInstanceId = "2.16.840.1.113883.2.9.2.120.4.4.030702.TSTSMN63A01F205H.20220325112426.OQlvTq1J.dead66852ddb42dbbdf3556bcd87be02^^^^urn:ihe:iti:xdw:2013:workflowInstanceId";
        edsClient.sendData(workFlowInstanceId);

    }



}
