package it.finanze.sanita.fse2.ms.gtwpublisher.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.AccreditationSimulationCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka.*;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.*;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.*;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IAccreditamentoSimulationSRV;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.impl.KafkaSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(KafkaSRV.class)
@TestPropertySource(properties = "spring.application.name=publisher-isolation")
class KafkaIsolationTest {
    @Autowired private KafkaSRV service;
    @MockitoBean private IEdsClient eds;
    @MockitoBean private KafkaTopicCFG topics;
    @MockitoBean private KafkaConsumerPropertiesCFG consumer;
    @MockitoBean private KafkaProducerPropertiesCFG producer;
    @MockitoBean private IAccreditamentoSimulationSRV simulation;
    @MockitoBean private AccreditationSimulationCFG simulationConfig;
    @MockitoBean(name = "txkafkatemplate") private KafkaTemplate<String, String> tx;
    @MockitoBean(name = "notxkafkatemplate") private KafkaTemplate<String, String> noTx;

    @BeforeEach
    void defaults() {
        clearInvocations(tx, noTx);
        when(topics.getStatusManagerTopic()).thenReturn("status");
        when(consumer.getNRetry()).thenReturn(2);
        when(eds.sendPublicationData(any(), any())).thenReturn(new EdsTraceResponseDTO(true, null, null));
        when(eds.sendReplaceData(any())).thenReturn(new EdsTraceResponseDTO(true, null, null));
    }

    private ConsumerRecord<String, String> record(String operation) {
        return new ConsumerRecord<>("input", 0, 0, "key", "{\"workflowInstanceId\":\"workflow\","
                + "\"idDoc\":\"document\",\"edsDPOperation\":\"" + operation + "\"}");
    }

    private JsonNode status() throws Exception {
        ArgumentCaptor<ProducerRecord<String, String>> sent = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(noTx).send(sent.capture());
        assertEquals("status", sent.getValue().topic());
        return new ObjectMapper().readTree(sent.getValue().value());
    }

    @Test
    void publishesToUarAndEmitsCorrelatedSuccess() throws Exception {
        service.basicListenerIndexer(record("PUBLISH"), 1);
        ArgumentCaptor<IndexerValueDTO> request = ArgumentCaptor.forClass(IndexerValueDTO.class);
        verify(eds).sendPublicationData(request.capture(), eq(DestinationEnum.SEND_TO_UAR));
        assertEquals("document", request.getValue().getIdDoc());
        assertEquals("workflow", request.getValue().getWorkflowInstanceId());
        assertEquals(DestinationEnum.SEND_TO_UAR, request.getValue().getDestination());
        JsonNode result = status();
        assertEquals("SUCCESS", result.get("eventStatus").asText());
        assertEquals("SEND_TO_UAR", result.get("eventType").asText());
        assertEquals("publisher-isolation", result.get("microserviceName").asText());
        assertNotNull(result.get("eventDate"));
        verifyNoInteractions(simulation, tx);
    }

    @Test
    void replacesAndRunsEnabledSimulation() throws Exception {
        when(simulationConfig.isEnableCheck()).thenReturn(true);
        service.basicListenerIndexer(record("REPLACE"), 1);
        verify(simulation).runSimulation("document");
        verify(eds).sendReplaceData(any());
        verify(eds, never()).sendPublicationData(any(), any());
        assertEquals("SUCCESS", status().get("eventStatus").asText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{broken", "null"})
    void rejectsMalformedPayloadBeforeCallingEds(String payload) throws Exception {
        assertThrows(BlockingEdsException.class, () -> service.basicListenerIndexer(
                new ConsumerRecord<>("input", 0, 0, "key", payload), 1));
        assertEquals("DESERIALIZE", status().get("eventType").asText());
        verifyNoInteractions(eds);
    }

    @Test
    void retriesUnclassifiedFailuresThenReportsExhaustion() throws Exception {
        when(eds.sendPublicationData(any(), any())).thenThrow(new IllegalStateException("offline"));
        assertEquals("offline", assertThrows(BlockingEdsException.class,
                () -> service.basicListenerIndexer(record("PUBLISH"), 1)).getMessage());
        verify(eds, times(3)).sendPublicationData(any(), any());
        assertEquals("BLOCKING_ERROR_MAX_RETRY", status().get("eventStatus").asText());
    }

    @Test
    void retriesTransientUnknownFailureAndCanRecover() throws Exception {
        when(eds.sendPublicationData(any(), any())).thenThrow(new IllegalStateException("first attempt"))
                .thenReturn(new EdsTraceResponseDTO(true, null, null));
        service.basicListenerIndexer(record("PUBLISH"), 1);
        verify(eds, times(2)).sendPublicationData(any(), any());
        assertEquals("SUCCESS", status().get("eventStatus").asText());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 6})
    void classifiedFailureIsRethrownWithoutInternalRetry(int delivery) throws Exception {
        BlockingEdsException failure = new BlockingEdsException("rejected");
        when(eds.sendPublicationData(any(), any())).thenThrow(failure);
        when(consumer.asExceptionType(failure)).thenReturn(java.util.Optional.of(EventStatusEnum.BLOCKING_ERROR));
        assertSame(failure, assertThrows(BlockingEdsException.class,
                () -> service.basicListenerIndexer(record("PUBLISH"), delivery)));
        verify(eds).sendPublicationData(any(), any());
        if (delivery == 1) assertEquals("BLOCKING_ERROR", status().get("eventStatus").asText());
        else verifyNoInteractions(noTx);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void negativeOrMissingEdsOutcomeIsNotSuccess(boolean missing) throws Exception {
        when(eds.sendPublicationData(any(), any())).thenReturn(new EdsTraceResponseDTO(missing ? null : false, null, "rejected"));
        assertThrows(BlockingEdsException.class, () -> service.basicListenerIndexer(record("PUBLISH"), 1));
        assertEquals("BLOCKING_ERROR_MAX_RETRY", status().get("eventStatus").asText());
    }

    @Test
    void wrapsStatusTransportFailure() {
        when(noTx.send(any(ProducerRecord.class))).thenThrow(new IllegalStateException("producer unavailable"));
        assertThrows(BusinessException.class, () -> service.sendStatusMessage("workflow",
                EventTypeEnum.SEND_TO_UAR, EventStatusEnum.SUCCESS, "done"));
    }

    @Test
    void transactionalSendReturnsMetadata() {
        when(producer.getTransactionalId()).thenReturn("transaction");
        RecordMetadata metadata = mock(RecordMetadata.class);
        when(tx.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(new SendResult<>(
                new ProducerRecord<>("output", "key", "value"), metadata)));
        when(tx.executeInTransaction(any())).thenAnswer(invocation -> {
            KafkaOperations.OperationsCallback<String, String, Object> callback = invocation.getArgument(0);
            return callback.doInOperations(tx);
        });
        assertSame(metadata, service.sendMessage("output", "key", "value"));
        verifyNoInteractions(noTx);
    }

    @Test
    void transactionFailureIsWrapped() {
        when(producer.getTransactionalId()).thenReturn("transaction");
        when(tx.executeInTransaction(any())).thenAnswer(invocation -> {
            KafkaOperations.OperationsCallback<String, String, Object> callback = invocation.getArgument(0);
            return callback.doInOperations(tx);
        });
        when(tx.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("failed")));
        assertThrows(BusinessException.class, () -> service.sendMessage("output", "key", "value"));
    }
}
