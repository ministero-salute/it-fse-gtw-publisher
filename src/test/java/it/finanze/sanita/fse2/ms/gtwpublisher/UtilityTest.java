/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtwpublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;

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
		DocumentResponseDTO dto2 = new DocumentResponseDTO(new LogTraceInfoDTO(TEST_SPAN_ID, TEST_TRACE_ID), "transaction_id");
		dto.setSpanID(TEST_SPAN_ID); 
		dto.setTraceID(TEST_TRACE_ID); 
		
		LogTraceInfoDTO logTraceInfoDto = new LogTraceInfoDTO(TEST_SPAN_ID, TEST_TRACE_ID); 
		ResponseDTO responseDto = new ResponseDTO(logTraceInfoDto); 
		
		String jsonAsString = JsonUtility.objectToJson(dto); 
		String jsonAsString2 = JsonUtility.objectToJson(dto2); 
		KafkaMessageDTO convertedDto = JsonUtility.jsonToObject(jsonAsString, KafkaMessageDTO.class); 
		KafkaMessageDTO convertedDto2 = JsonUtility.jsonToObject(jsonAsString2, KafkaMessageDTO.class); 
		
		String jsonAsStringResponseDto = JsonUtility.objectToJson(responseDto); 
		KafkaMessageDTO convertedResponseDto = JsonUtility.jsonToObject(jsonAsStringResponseDto, KafkaMessageDTO.class); 
		
		assertNull(convertedDto); 
		assertNull(convertedDto2); 
		assertNull(convertedResponseDto); 
		
	}

    @Test
    @DisplayName("jsonToObject OK")
    void testJsonToObjectOk() {
        String input = "{\"identificativo\":\"stub\",\"flagPresaInCarico\":false}";
        Map<String, Object> map = new HashMap<>();
        map.put("identificativo", "stub");
        map.put("flagPresaInCarico", false);
        assertEquals(map, JsonUtility.jsonToObject(input, Map.class));
    }

    @Test
    @DisplayName("objectToJson malformedInput")
    void testObjectToJsonMalformedInput() {
        Map<String, Object> input = new HashMap<>();
        input.put(null, null);
        assertEquals("", JsonUtility.objectToJson(input));
    }

    @Test
    @DisplayName("objectToJson OK")
    void testObjectToJsonOK() {
        Map<String, Object> input = new HashMap<>();
        input.put("key", "value");
        assertEquals("{\"key\":\"value\"}", JsonUtility.objectToJson(input));
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
		assertThrows(ResourceAccessException.class, ()->edsClient.sendPublicationData(new IndexerValueDTO(workflowInstanceId, "idDoc", ProcessorOperationEnum.PUBLISH), priorityTypeEnum));
	}
	
	@Test
	@DisplayName("Generate UUID Test")
	void generateUuidTest() {
		String uuid = StringUtility.generateUUID(); 
		
		assertEquals(String.class, uuid.getClass()); 
	}

} 


