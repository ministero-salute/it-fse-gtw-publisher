package it.finanze.sanita.fse2.ms.gtwpublisher.isolation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.*;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.*;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.*;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.impl.AccreditamentoSimulationSRV;

@Tag("ISOLATION")
class UtilitiesIsolationTest {
    public static class BrokenJson {
        public String getValue() { throw new IllegalStateException("cannot serialize"); }
    }

    @Test
    void stringHashAndEncodingHaveStableResults() {
        assertTrue(StringUtility.isNullOrEmpty(null));
        assertTrue(StringUtility.isNullOrEmpty(""));
        assertFalse(StringUtility.isNullOrEmpty(" "));
        assertEquals("YWJj", StringUtility.encodeBase64(new byte[]{97, 98, 99}));
        assertEquals("ungWv48Bz+pBQUDeXa4iI7ADYaOWF3qctBD/YfIAFa0=", StringUtility.encodeSHA256B64("abc"));
        assertThrows(BusinessException.class, () -> StringUtility.encodeSHA256B64(null));
        assertNotNull(UUID.fromString(StringUtility.generateUUID()));
    }

    @Test
    void jsonRoundTripsAndDocumentsFailureContracts() {
        Map<String, String> value = Map.of("key", "value");
        assertEquals(value, StringUtility.fromJSON(StringUtility.toJSON(value), Map.class));
        assertEquals(value, JsonUtility.jsonToObject(JsonUtility.objectToJson(value), Map.class));
        assertEquals(value, JsonUtility.clone(value, Map.class));
        assertNull(JsonUtility.jsonToObject("{broken", Map.class));
        assertEquals("", JsonUtility.objectToJson(new BrokenJson()));
        assertThrows(BusinessException.class, () -> StringUtility.toJSONJackson(new BrokenJson()));
        assertEquals("{\"key\":\"value\"}", StringUtility.toJSONJackson(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "TEST-local", "dev", "docker", "prod", "ISOLATION"})
    void recognizesOnlyExpectedProfiles(String active) {
        ProfileUtility profiles = new ProfileUtility();
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(active);
        ReflectionTestUtils.setField(profiles, "environment", environment);
        assertEquals(active.toLowerCase().contains("test"), profiles.isTestProfile());
        assertEquals(active.equals("dev") || active.equals("docker"), profiles.isDevOrDockerProfile());
    }

    @Test
    void absentAndEmptyEnvironmentProfilesAreSafe() {
        ProfileUtility profiles = new ProfileUtility();
        assertFalse(profiles.isTestProfile());
        assertFalse(profiles.isDevOrDockerProfile());
        ReflectionTestUtils.setField(profiles, "environment", new MockEnvironment());
        assertFalse(profiles.isTestProfile());
        assertFalse(profiles.isDevOrDockerProfile());
    }

    @Test
    void accreditationRecognizesCrashPrefixButNotOrdinaryDocuments() {
        AccreditamentoSimulationSRV simulation = new AccreditamentoSimulationSRV();
        assertNull(simulation.runSimulation("ordinary-document"));
        assertThrows(BlockingEdsException.class, () -> simulation.runSimulation("CRASH_EDS-document"));
        assertEquals(AccreditamentoPrefixEnum.CRASH_EDS, AccreditamentoPrefixEnum.get("crash_eds"));
        assertNull(AccreditamentoPrefixEnum.get("unknown"));
        assertNull(AccreditamentoPrefixEnum.getStartWith("crash_eds"));
    }

    @Test
    void eventDestinationMappingRejectsUnsupportedValues() {
        assertEquals(EventTypeEnum.SEND_TO_UAR, EventTypeEnum.getEventTypeFromDestination("send_to_uar"));
        assertThrows(IllegalArgumentException.class, () -> EventTypeEnum.getEventTypeFromDestination("SEND_TO_UDP"));
    }
}
