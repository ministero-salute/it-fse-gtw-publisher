/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher;

import com.google.gson.Gson;
import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
		
		final String value = new Gson().toJson(new IndexerValueDTO(TestConstants.testWorkflowInstanceId, "String", ProcessorOperationEnum.PUBLISH));

		ConsumerRecord<String, String> recordLow = new ConsumerRecord<>(topicLow, 1, 0, StringUtility.generateUUID(), value);
		ConsumerRecord<String, String> recordMedium = new ConsumerRecord<>(topicMedium, 1, 0, StringUtility.generateUUID(), value);
		ConsumerRecord<String, String> recordHigh = new ConsumerRecord<>(topicHigh, 1, 0, StringUtility.generateUUID(), value);

		EdsTraceResponseDTO mockResponse = new EdsTraceResponseDTO();
		mockResponse.setEsito(true);

		Mockito.doReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK)).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(EdsTraceResponseDTO.class));

		assertDoesNotThrow(()->kafkaSRV.lowPriorityListenerIndexer(recordLow, 0));
		assertDoesNotThrow(()->kafkaSRV.mediumPriorityListenerIndexer(recordMedium, 0));
		assertDoesNotThrow(()->kafkaSRV.highPriorityListenerIndexer(recordHigh, 0));
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

		final String value = new Gson().toJson(new IndexerValueDTO(TestConstants.testWorkflowInstanceId, "String", ProcessorOperationEnum.REPLACE));

		ConsumerRecord<String, String> recordLow = new ConsumerRecord<>(topicLow, 1, 0, StringUtility.generateUUID(), value);

		EdsTraceResponseDTO mockResponse = new EdsTraceResponseDTO();
		mockResponse.setEsito(true);

		Mockito.doReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK)).when(restTemplate)
				.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(EdsTraceResponseDTO.class));

		assertDoesNotThrow(()->kafkaSRV.lowPriorityListenerIndexer(recordLow, 0));
	}

}
