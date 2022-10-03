package it.finanze.sanita.fse2.ms.gtwpublisher;

import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ErrorLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ResultLogEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class EnumsTest {

    @Test
    @DisplayName("testErrorLogEnums")
    void testErrorLogEnums() {
        String code = "KO_EDS";
        assertEquals(code, ErrorLogEnum.KO_EDS.getCode());
    }

    @Test
    @DisplayName("testEventTypeEnums")
    void testEventTypeEnums() {
        String code = EventTypeEnum.SEND_TO_EDS.name();
        assertEquals(code, EventTypeEnum.SEND_TO_EDS.name());
    }

    @Test
    @DisplayName("testResultLogEnum")
    void testResultLogEnum() {
        String code = ResultLogEnum.KO.getCode();
        assertEquals(code, ResultLogEnum.KO.getCode());
    }
}
