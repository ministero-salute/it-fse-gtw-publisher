package it.finanze.sanita.fse2.ms.gtwpublisher.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.health.KafkaHealthIndicator;
import it.finanze.sanita.fse2.ms.gtwpublisher.controller.impl.LivenessCheckCTL;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({KafkaHealthIndicator.class, LivenessCheckCTL.class})
class HealthIsolationTest {
    @Autowired private KafkaHealthIndicator health;
    @Autowired private LivenessCheckCTL controller;
    @MockitoBean private AdminClient admin;
    private KafkaFuture future;

    @BeforeEach
    void setup() {
        ListTopicsResult topics = mock(ListTopicsResult.class);
        future = mock(KafkaFuture.class);
        when(admin.listTopics()).thenReturn(topics);
        when(topics.listings()).thenReturn(future);
    }

    @Test
    void livenessHttpEndpointDoesNotDependOnKafka() throws Exception {
        MockMvcBuilders.standaloneSetup(controller).build().perform(get("/status"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        verifyNoInteractions(admin);
    }

    @Test
    void reportsKafkaUp() throws Exception {
        when(future.get()).thenReturn(List.of());
        assertEquals(Status.UP, health.health().getStatus());
        verify(future).get();
    }

    @Test
    void reportsKafkaDownWithFailureDetails() throws Exception {
        when(future.get()).thenThrow(new ExecutionException(new IllegalStateException("offline")));
        var result = health.health();
        assertEquals(Status.DOWN, result.getStatus());
        assertTrue(result.getDetails().get("error").toString().contains("offline"));
    }

    @Test
    void restoresInterruptFlagWhenHealthCheckIsInterrupted() throws Exception {
        when(future.get()).thenThrow(new InterruptedException("interrupted"));
        try {
            assertEquals(Status.DOWN, health.health().getStatus());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
}
