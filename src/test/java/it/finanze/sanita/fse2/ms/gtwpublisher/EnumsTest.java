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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.AccreditamentoPrefixEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ErrorLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventSourceEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ResultLogEnum;

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
        String code = EventTypeEnum.SEND_TO_EDS.getName();
        assertEquals(code, EventTypeEnum.SEND_TO_EDS.getName());
    }
    
    @Test
    @DisplayName("testEventStatusEnums")
    void testEventStatusEnums() {
        String blockingErrorName = EventStatusEnum.BLOCKING_ERROR.getName();
        assertEquals(blockingErrorName, EventStatusEnum.BLOCKING_ERROR.getName());
    }

    @Test
    @DisplayName("testResultLogEnum")
    void testResultLogEnum() {
        String code = ResultLogEnum.KO.getCode();
        assertEquals(code, ResultLogEnum.KO.getCode());
    }
    
    @Test
    @DisplayName("testEventSourceEnum")
    void testEventSourceEnum() {
        String dispatcherName = EventSourceEnum.DISPATCHER.getName();
        String dispatcherDesc = EventSourceEnum.DISPATCHER.getDescription();
        assertEquals(dispatcherName, EventSourceEnum.DISPATCHER.getName());
        assertEquals(dispatcherDesc, EventSourceEnum.DISPATCHER.getDescription());
        
        String indexerName = EventSourceEnum.INDEXER.getName();
        String indexerdispatcherDesc = EventSourceEnum.INDEXER.getDescription();
        assertEquals(indexerName, EventSourceEnum.INDEXER.getName());
        assertEquals(indexerdispatcherDesc, EventSourceEnum.INDEXER.getDescription());
    }
    
    @Test
    @DisplayName("testOperationLogEnum")
    void testOperationLogEnum() {
    	String code = OperationLogEnum.SEND_EDS.getCode();
    	assertEquals(code, OperationLogEnum.SEND_EDS.getCode());
    }
    
    @Test
    @DisplayName("testAccreditamentoPrefixEnum")
    void testAccreditamentoPrefixEnum() {
    	String prefix = AccreditamentoPrefixEnum.CRASH_EDS.getPrefix();
    	assertEquals(prefix, AccreditamentoPrefixEnum.CRASH_EDS.getPrefix());
    	assertEquals(AccreditamentoPrefixEnum.CRASH_EDS, AccreditamentoPrefixEnum.get(prefix));
    }
}
