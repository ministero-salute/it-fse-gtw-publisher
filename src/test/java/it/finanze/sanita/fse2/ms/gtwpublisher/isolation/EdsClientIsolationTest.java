package it.finanze.sanita.fse2.ms.gtwpublisher.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtwpublisher.client.impl.EdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.*;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.*;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(EdsClient.class)
class EdsClientIsolationTest {
    @Autowired private EdsClient client;
    @MockitoBean private RestTemplate http;
    @MockitoBean private MicroservicesURLCFG urls;
    private final IndexerValueDTO request = new IndexerValueDTO("workflow", "document", ProcessorOperationEnum.REPLACE, DestinationEnum.SEND_TO_UAR);
    private final EdsTraceResponseDTO response = new EdsTraceResponseDTO(true, null, null);

    @BeforeEach
    void setup() {
        when(urls.getEdsClientHost()).thenReturn("http://eds.invalid");
        when(http.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(EdsTraceResponseDTO.class)))
                .thenReturn(ResponseEntity.ok(response));
    }

    @Test
    void publicationMapsBodyDestinationAndHeaders() {
        assertSame(response, client.sendPublicationData(request, DestinationEnum.SEND_TO_UDP));
        ArgumentCaptor<HttpEntity> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(http).exchange(eq("http://eds.invalid/v1/documents"), eq(HttpMethod.POST), entity.capture(), eq(EdsTraceResponseDTO.class));
        PublicationRequestBodyDTO body = (PublicationRequestBodyDTO) entity.getValue().getBody();
        assertEquals("workflow", body.getWorkflowInstanceId());
        assertEquals("document", body.getIdentificativoDoc());
        assertEquals(DestinationEnum.SEND_TO_UDP, body.getDestination());
        assertEquals(MediaType.APPLICATION_JSON, entity.getValue().getHeaders().getContentType());
    }

    @Test
    void replacementUsesPutAndSerializesOriginalRequest() {
        assertSame(response, client.sendReplaceData(request));
        ArgumentCaptor<HttpEntity> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(http).exchange(eq("http://eds.invalid/v1/documents/document"), eq(HttpMethod.PUT), entity.capture(), eq(EdsTraceResponseDTO.class));
        IndexerValueDTO body = StringUtility.fromJSON((String) entity.getValue().getBody(), IndexerValueDTO.class);
        assertEquals(request.getIdDoc(), body.getIdDoc());
        assertEquals(request.getWorkflowInstanceId(), body.getWorkflowInstanceId());
        assertEquals(request.getEdsDPOperation(), body.getEdsDPOperation());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void missingDocumentOrWorkflowIsRejectedWithoutHttp(String missing) {
        request.setIdDoc(missing);
        assertThrows(BusinessException.class, () -> client.sendReplaceData(request));
        request.setIdDoc("document");
        request.setWorkflowInstanceId(missing);
        assertThrows(BusinessException.class, () -> client.sendReplaceData(request));
        verifyNoInteractions(http);
    }

    @Test
    void connectionFailuresArePreservedForRetryClassification() {
        ResourceAccessException failure = new ResourceAccessException("offline");
        when(http.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(EdsTraceResponseDTO.class))).thenThrow(failure);
        assertSame(failure, assertThrows(ResourceAccessException.class, () -> client.sendPublicationData(request, DestinationEnum.SEND_TO_UAR)));
        assertSame(failure, assertThrows(ResourceAccessException.class, () -> client.sendReplaceData(request)));
    }

    @Test
    void genericFailureIsPreservedForPublicationAndWrappedForReplacement() {
        IllegalStateException failure = new IllegalStateException("unexpected response");
        when(http.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(EdsTraceResponseDTO.class))).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> client.sendPublicationData(request, DestinationEnum.SEND_TO_UAR)));
        assertSame(failure, assertThrows(BusinessException.class, () -> client.sendReplaceData(request)).getCause());
    }
}
