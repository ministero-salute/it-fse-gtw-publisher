package it.finanze.sanita.fse2.ms.gtwpublisher;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ILogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.logging.KafkaLoggerHelper;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class GtwPublisherMsApplicationTests {

	@Autowired
	private KafkaLoggerHelper kafkaLogger;

	@Test
	void contextLoads() {
	    kafkaLogger.trace("messaggio per elk", OperationLogEnum.SEND_EDS, ResultLogEnum.OK, new Date());
	    kafkaLogger.info("messaggio per elk", OperationLogEnum.SEND_EDS, ResultLogEnum.OK, new Date());
	    kafkaLogger.debug("messaggio ko", OperationLogEnum.SEND_EDS, ResultLogEnum.KO, new Date());
	    kafkaLogger.warn("messaggio ok", OperationLogEnum.SEND_EDS, ResultLogEnum.OK, new Date());
	    kafkaLogger.error("messaggio errore", OperationLogEnum.SEND_EDS, ResultLogEnum.OK, new Date(), new ILogEnum() {
			
			@Override
			public String getDescription() {
				return null;
			}
			
			@Override
			public String getCode() {
				return null;
			}
		});

	}

}
