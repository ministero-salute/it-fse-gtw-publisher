package it.finanze.sanita.fse2.ms.gtwpublisher;

import com.google.gson.Gson;
import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class KafkaTest {
	
	@Autowired
	private IKafkaSRV kafkaSRV;

	@Autowired
	private KafkaTopicCFG kafkaTopicCFG;

	@Autowired
	private IEdsClient edsClient;

    @SpyBean
	private RestTemplate restTemplate;

	@Test
	@Description("Publish - Success test on indexer listener")
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
		
		final String value = new Gson().toJson(new IndexerValueDTO(TestConstants.testWorkflowInstanceId, "String", ProcessorOperationEnum.PUBLISH));

		ConsumerRecord<String, String> recordLow = new ConsumerRecord<String,String>(topicLow, 1, 0, StringUtility.generateUUID(), value);
		ConsumerRecord<String, String> recordMedium = new ConsumerRecord<String,String>(topicMedium, 1, 0, StringUtility.generateUUID(), value);
		ConsumerRecord<String, String> recordHigh = new ConsumerRecord<String,String>(topicHigh, 1, 0, StringUtility.generateUUID(), value);

		EdsPublicationResponseDTO mockResponse = new EdsPublicationResponseDTO();
		mockResponse.setEsito(true);

		Mockito.doReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK)).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(EdsPublicationResponseDTO.class));

		assertDoesNotThrow(()->kafkaSRV.lowPriorityListenerIndexer(recordLow, headers));
		assertDoesNotThrow(()->kafkaSRV.mediumPriorityListenerIndexer(recordMedium, headers));
		assertDoesNotThrow(()->kafkaSRV.highPriorityListenerIndexer(recordHigh, headers));
	}

	@Test
	@Description("Publish - Success test on dispatcher listener")
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
		
		final String value = new Gson().toJson(new IndexerValueDTO(TestConstants.testWorkflowInstanceId, "String", ProcessorOperationEnum.PUBLISH));

		ConsumerRecord<String, String> recordLow = new ConsumerRecord<String,String>(topicLow, 1, 0, StringUtility.generateUUID(), value);
		ConsumerRecord<String, String> recordMedium = new ConsumerRecord<String,String>(topicMedium, 1, 0, StringUtility.generateUUID(), value);
		ConsumerRecord<String, String> recordHigh = new ConsumerRecord<String,String>(topicHigh, 1, 0, StringUtility.generateUUID(), value);

		EdsPublicationResponseDTO mockResponse = new EdsPublicationResponseDTO();
		mockResponse.setEsito(true);

		Mockito.doReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK)).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(EdsPublicationResponseDTO.class));

		assertDoesNotThrow(()->kafkaSRV.lowPriorityListenerDispatcher(recordLow, headers));
		assertDoesNotThrow(()->kafkaSRV.mediumPriorityListenerDispatcher(recordMedium, headers));
		assertDoesNotThrow(()->kafkaSRV.highPriorityListenerDispatcher(recordHigh, headers));
	}

	@Test
	@Description("Publish - error test on indexer listener - do nothing")
	void kafkaListenerIndexerErrorTest() {
		String topicLow = kafkaTopicCFG.getIndexerPublisherLowPriorityTopic();
		String topicMedium = kafkaTopicCFG.getIndexerPublisherMediumPriorityTopic();
		String topicHigh = kafkaTopicCFG.getIndexerPublisherHighPriorityTopic();

		Map<String, Object> map = new HashMap<>();
		MessageHeaders headers = new MessageHeaders(map);

		Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

		records.put(new TopicPartition(topicLow, 0), new ArrayList<>());
		records.put(new TopicPartition(topicMedium, 0), new ArrayList<>());
		records.put(new TopicPartition(topicHigh, 0), new ArrayList<>());

		final String value = "{\"workflowInstanceId\":\"wii1\",\"idDoc\":\"id1\",\"edsDPOperation\":\"PUBLISH\"}";

		ConsumerRecord<String, String> recordLow = new ConsumerRecord<String,String>(topicLow, 1, 0, StringUtility.generateUUID(), value);
		ConsumerRecord<String, String> recordMedium = new ConsumerRecord<String,String>(topicMedium, 1, 0, StringUtility.generateUUID(), value);
		ConsumerRecord<String, String> recordHigh = new ConsumerRecord<String,String>(topicHigh, 1, 0, StringUtility.generateUUID(), value);

		EdsPublicationResponseDTO mockResponse = new EdsPublicationResponseDTO();
		mockResponse.setEsito(false);
		mockResponse.setMessageError("Errore generico");

		Mockito.doReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK)).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(EdsPublicationResponseDTO.class));

		assertDoesNotThrow(()->kafkaSRV.lowPriorityListenerIndexer(recordLow, headers));
		assertDoesNotThrow(()->kafkaSRV.mediumPriorityListenerIndexer(recordMedium, headers));
		assertDoesNotThrow(()->kafkaSRV.highPriorityListenerIndexer(recordHigh, headers));
	}

	@Test
	@Description("Publish - exceptions test on indexer listener")
	void kafkaListenerIndexerAllExceptionTest() {
		String topicLow = kafkaTopicCFG.getIndexerPublisherLowPriorityTopic();
		String topicMedium = kafkaTopicCFG.getIndexerPublisherMediumPriorityTopic();
		String topicHigh = kafkaTopicCFG.getIndexerPublisherHighPriorityTopic();

		Map<String, Object> map = new HashMap<>();
		MessageHeaders headers = new MessageHeaders(map);

		Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

		records.put(new TopicPartition(topicLow, 0), new ArrayList<>());
		records.put(new TopicPartition(topicMedium, 0), new ArrayList<>());
		records.put(new TopicPartition(topicHigh, 0), new ArrayList<>());

		final String value = "{\"workflowInstanceId\":\"wii1\",\"idDoc\":\"id1\",\"edsDPOperation\":\"PUBLISH\"}";

		ConsumerRecord<String, String> recordLow = new ConsumerRecord<String,String>(topicLow, 1, 0, StringUtility.generateUUID(), value);
		ConsumerRecord<String, String> recordMedium = new ConsumerRecord<String,String>(topicMedium, 1, 0, StringUtility.generateUUID(), value);
		ConsumerRecord<String, String> recordHigh = new ConsumerRecord<String,String>(topicHigh, 1, 0, StringUtility.generateUUID(), value);

		EdsPublicationResponseDTO mockResponse = new EdsPublicationResponseDTO();
		mockResponse.setEsito(true);

		Mockito.doThrow(new ResourceAccessException("")).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(EdsPublicationResponseDTO.class));

		assertThrows(ResourceAccessException.class, ()->kafkaSRV.lowPriorityListenerIndexer(recordLow, headers));
		assertThrows(ResourceAccessException.class, ()->kafkaSRV.mediumPriorityListenerIndexer(recordMedium, headers));
		assertThrows(ResourceAccessException.class, ()->kafkaSRV.highPriorityListenerIndexer(recordHigh, headers));

		Mockito.doThrow(new ConnectionRefusedException("", "")).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(EdsPublicationResponseDTO.class));

		assertThrows(ConnectionRefusedException.class, ()->kafkaSRV.lowPriorityListenerIndexer(recordLow, headers));
		assertThrows(ConnectionRefusedException.class, ()->kafkaSRV.mediumPriorityListenerIndexer(recordMedium, headers));
		assertThrows(ConnectionRefusedException.class, ()->kafkaSRV.highPriorityListenerIndexer(recordHigh, headers));

		Mockito.doThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY)).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(EdsPublicationResponseDTO.class));

		assertThrows(BusinessException.class, ()->kafkaSRV.lowPriorityListenerIndexer(recordLow, headers));
		assertThrows(BusinessException.class, ()->kafkaSRV.mediumPriorityListenerIndexer(recordMedium, headers));
		assertThrows(BusinessException.class, ()->kafkaSRV.highPriorityListenerIndexer(recordHigh, headers));
	}

    @Test
	@Description("Send data to gtw-eds-client")
    @Disabled("Real test")
	void testSendDataToClientMicroservice() {
        // To run this test change the edsClient @MockBean to @Autowired at the top of this class
        // paste a workflowInstanceId present in your ini_eds_invocation mongo collection

        String workFlowInstanceId = "2.16.840.1.113883.2.9.2.120.4.4.030702.TSTSMN63A01F205H.20220325112426.OQlvTq1J.dead66852ddb42dbbdf3556bcd87be02^^^^urn:ihe:iti:xdw:2013:workflowInstanceId";
        edsClient.sendPublicationData(new IndexerValueDTO(workFlowInstanceId, "idDoc", ProcessorOperationEnum.PUBLISH), PriorityTypeEnum.HIGH);

    }

	@Test
	@Description("Replace - Success test on indexer listener")
	void kafkaReplaceListenerIndexerSuccessTest() {
		String topicLow = kafkaTopicCFG.getIndexerPublisherLowPriorityTopic();

		Map<String, Object> map = new HashMap<>();
		MessageHeaders headers = new MessageHeaders(map);

		Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

		records.put(new TopicPartition(topicLow, 0), new ArrayList<>());

		final String value = new Gson().toJson(new IndexerValueDTO(TestConstants.testWorkflowInstanceId, "String", ProcessorOperationEnum.REPLACE));

		ConsumerRecord<String, String> recordLow = new ConsumerRecord<String,String>(topicLow, 1, 0, StringUtility.generateUUID(), value);

		EdsPublicationResponseDTO mockResponse = new EdsPublicationResponseDTO();
		mockResponse.setEsito(true);

		Mockito.doReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK)).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(EdsPublicationResponseDTO.class));

		assertDoesNotThrow(()->kafkaSRV.lowPriorityListenerIndexer(recordLow, headers));
	}

	@Test
	@Description("Replace - error test on indexer listener - do nothing")
	void kafkaReplaceListenerIndexerErrorTest() {
		String topicLow = kafkaTopicCFG.getIndexerPublisherLowPriorityTopic();

		Map<String, Object> map = new HashMap<>();
		MessageHeaders headers = new MessageHeaders(map);

		Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

		records.put(new TopicPartition(topicLow, 0), new ArrayList<>());

		final String value = "{\"workflowInstanceId\":\"wii1\",\"idDoc\":\"id1\",\"edsDPOperation\":\"REPLACE\"}";

		ConsumerRecord<String, String> recordLow = new ConsumerRecord<String,String>(topicLow, 1, 0, StringUtility.generateUUID(), value);

		EdsPublicationResponseDTO mockResponse = new EdsPublicationResponseDTO();
		mockResponse.setEsito(false);
		mockResponse.setMessageError("Errore generico");

		Mockito.doReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK)).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(EdsPublicationResponseDTO.class));

		assertDoesNotThrow(()->kafkaSRV.lowPriorityListenerIndexer(recordLow, headers));
	}

	@Test
	@Description("Replace - exceptions test on indexer listener")
	void kafkaReplaceListenerIndexerAllExceptionTest() {
		String topicLow = kafkaTopicCFG.getIndexerPublisherLowPriorityTopic();

		Map<String, Object> map = new HashMap<>();
		MessageHeaders headers = new MessageHeaders(map);

		Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

		records.put(new TopicPartition(topicLow, 0), new ArrayList<>());

		final String value = "{\"workflowInstanceId\":\"wii1\",\"idDoc\":\"id1\",\"edsDPOperation\":\"REPLACE\"}";

		ConsumerRecord<String, String> recordLow = new ConsumerRecord<String,String>(topicLow, 1, 0, StringUtility.generateUUID(), value);

		EdsPublicationResponseDTO mockResponse = new EdsPublicationResponseDTO();
		mockResponse.setEsito(true);

		Mockito.doThrow(new ResourceAccessException("")).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(EdsPublicationResponseDTO.class));

		assertThrows(ResourceAccessException.class, ()->kafkaSRV.lowPriorityListenerIndexer(recordLow, headers));

		Mockito.doThrow(new ConnectionRefusedException("", "")).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(EdsPublicationResponseDTO.class));

		assertThrows(ConnectionRefusedException.class, ()->kafkaSRV.lowPriorityListenerIndexer(recordLow, headers));

		Mockito.doThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY)).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(EdsPublicationResponseDTO.class));

		assertThrows(BusinessException.class, ()->kafkaSRV.lowPriorityListenerIndexer(recordLow, headers));
	}
}
