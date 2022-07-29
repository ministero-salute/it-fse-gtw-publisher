package it.finanze.sanita.fse2.ms.gtwpublisher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.CurrentApplicationLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ErrorLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.Data;
import lombok.NoArgsConstructor;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class UtilityTest {
	
	@Autowired
	private IEdsClient edsClient;
	
	@Test
	@DisplayName("enumeration test ")
	void CurrentApplicationLogEnum() {			
			for(CurrentApplicationLogEnum entry : Arrays.asList(CurrentApplicationLogEnum.values())) {
				assertNotNull(entry.getCode());
				assertNotNull(entry.getDescription());
				}
			
			for(ErrorLogEnum entry : Arrays.asList(ErrorLogEnum.values())) {
				assertNotNull(entry.getCode());
				assertNotNull(entry.getDescription());
				}
			
			for(EventTypeEnum entry : Arrays.asList(EventTypeEnum.values())) {
				assertNotNull(entry.getName());
				}
			
			for(EventStatusEnum entry : Arrays.asList(EventStatusEnum.values())) {
				assertNotNull(entry.getName());
				}
			
			for(CurrentApplicationLogEnum entry : Arrays.asList(CurrentApplicationLogEnum.values())) {
				assertNotNull(entry.getCode());
				assertNotNull(entry.getDescription());
				}
			
			for(CurrentApplicationLogEnum entry : Arrays.asList(CurrentApplicationLogEnum.values())) {
				assertNotNull(entry.getCode());
				assertNotNull(entry.getDescription());
				}
	}
	
	
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
	
	@Data
	@NoArgsConstructor
	class KafkaMessageDTO{
		String message;
	}
	
	@Test
	@DisplayName("findAndSendToEdsByWorkflowInstanceId test Ko")
	void findAndSendToEdsByWorkflowInstanceIdKo() {
		String workflowInstanceId = null;
		assertThrows(BusinessException.class, ()->edsClient.sendData(workflowInstanceId));
	}
	
	
	
}