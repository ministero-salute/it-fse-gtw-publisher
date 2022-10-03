package it.finanze.sanita.fse2.ms.gtwpublisher;

import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.DocumentResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.JsonUtility;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class UtilityTest {
	
	public static final String TEST_MESSAGE = "test"; 
	
	public static final String TEST_SPAN_ID = "testSpanId"; 
	public static final String TEST_TRACE_ID = "testTraceId"; 

	
	
	@Autowired
	private IEdsClient edsClient;
	
	@Test
	@DisplayName("String utility null -> exception")
	void testStringUtilityException() throws Exception{
		
		boolean flag = StringUtility.isNullOrEmpty("");
		assertTrue(flag);
		
		boolean flag1 = StringUtility.isNullOrEmpty(null);
		assertTrue(flag1);
		
		boolean flag2 = StringUtility.isNullOrEmpty("aaaaa");
		assertFalse(flag2);
	}
	
	@Test
	@DisplayName("String utility null -> exception")
	void encodeSHA256B64Exception() throws Exception{
		
		//exception encode
		String convertion = null;
		try {
			convertion = StringUtility.encodeSHA256B64(null);
		} catch (Exception e) {
			Assertions.assertNull(convertion);
		}
		assertThrows(BusinessException.class, () -> StringUtility.encodeSHA256B64(null));
		//encode OK
		String convertion1 = null;
		convertion1 = StringUtility.encodeSHA256B64("aaa");
		assertNotNull(convertion1);
		
	}
	
	@Test
	@DisplayName("String utility -> OK")
	void encodeSHA256B64Ok() {
		//encode OK
		String convertion1 = null;
		convertion1 = StringUtility.encodeSHA256B64("aaa");
		assertNotNull(convertion1);
	}
	
	
	@Test
	@DisplayName("String utility HEX null -> exception")
	void encodeSHA256HEXException() throws Exception{
		
		//exception encode
		String convertion = null;
		try {
			convertion = StringUtility.encodeSHA256Hex(null);
		} catch (Exception e) {
			Assertions.assertNull(convertion);
		}
		assertThrows(BusinessException.class, () -> StringUtility.encodeSHA256Hex(null));
	}
	
	@Test
	@DisplayName("String utility 256 HEX-> OK")
	void encodeSHA256HEXOk() {
		//encode OK
		String strToConvert = "0";
		String convertion = StringUtility.encodeSHA256Hex(strToConvert);
		assertNotNull(convertion);
		//encode wrong case
		String strToConvert2 = "1aa";
		String convertion2 = StringUtility.encodeSHA256Hex(strToConvert2);
		assertNotNull(convertion2);
		
	}
 
	
	@Test
	@DisplayName("tojsonjackson test")
	void toJSONJacksonTest() {
		KafkaMessageDTO msg = new KafkaMessageDTO();
		assertNotNull(StringUtility.toJSONJackson(msg));
	} 
	
	@Test
	@DisplayName("Json Utility - Test OK")
	void jsonUtilityTestOk() {
		KafkaMessageDTO dto = new KafkaMessageDTO(); 
		dto.setMessage(TEST_MESSAGE); 
		
		String jsonAsString = JsonUtility.objectToJson(dto); 
		
		assertEquals(String.class, jsonAsString.getClass()); 		

	} 
	
	
	@Test
	@DisplayName("Json Utility - Test KO")
	void jsonUtilityTestKo() {
		DocumentResponseDTO dto = new DocumentResponseDTO(); 
		dto.setSpanID(TEST_SPAN_ID); 
		dto.setTraceID(TEST_TRACE_ID); 
		
		LogTraceInfoDTO logTraceInfoDto = new LogTraceInfoDTO(TEST_SPAN_ID, TEST_TRACE_ID); 
		ResponseDTO responseDto = new ResponseDTO(logTraceInfoDto); 
		
		String jsonAsString = JsonUtility.objectToJson(dto); 
		KafkaMessageDTO convertedDto = JsonUtility.jsonToObject(jsonAsString, KafkaMessageDTO.class); 
		
		String jsonAsStringResponseDto = JsonUtility.objectToJson(responseDto); 
		KafkaMessageDTO convertedResponseDto = JsonUtility.jsonToObject(jsonAsStringResponseDto, KafkaMessageDTO.class); 
		
		assertNull(convertedDto); 
		assertNull(convertedResponseDto); 
		
	} 
	

	
	@Data
	@NoArgsConstructor
	class KafkaMessageDTO{
		String message;
	}
	
	@Test
	@DisplayName("findAndSendToEdsByWorkflowInstanceId test Ko")
	void findAndSendToEdsByWorkflowInstanceIdKo() {
		String workflowInstanceId = null;
		PriorityTypeEnum priorityTypeEnum = PriorityTypeEnum.HIGH;
		assertThrows(BusinessException.class, ()->edsClient.sendPublicationData(new IndexerValueDTO(workflowInstanceId, "idDoc", ProcessorOperationEnum.PUBLISH), priorityTypeEnum));
	}
	
	@Test
	@DisplayName("Generate UUID Test")
	void generateUuidTest() {
		String uuid = StringUtility.generateUUID(); 
		
		assertEquals(String.class, uuid.getClass()); 
	}
	

} 


