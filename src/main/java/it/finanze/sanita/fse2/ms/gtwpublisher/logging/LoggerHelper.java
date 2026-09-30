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
package it.finanze.sanita.fse2.ms.gtwpublisher.logging;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtwpublisher.dto.LogDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ErrorLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoggerHelper {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("dd-MM-yyyy HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    private final Logger kafkaLog = LoggerFactory.getLogger("kafka-logger");

    @Value("${log.kafka-log.enable}")
    private boolean kafkaLogEnable;

    @Value("${spring.application.name}")
    private String microserviceName;

    public void sendToUar(String workflowInstanceId, String idDocumento, EventStatusEnum status, String message,
            Date startDate, String errorDescription) {

        if (!kafkaLogEnable) {
            log.warn("Structured Log disabled - Skipping log emission");
            return;
        }

        log.info("Structured log emission start at {}", System.currentTimeMillis());

        LogDTO logDTO = LogDTO.builder()
                .message(message)
                .operation(OperationLogEnum.SEND_TO_UAR.getCode())
                .op_result(status.getName())
                .op_timestamp_start(DATE_FORMAT.format(startDate.toInstant()))
                .op_timestamp_end(DATE_FORMAT.format(new Date().toInstant()))
                .op_error(errorDescription == null ? null : ErrorLogEnum.KO_EDS.getCode())
                .op_error_description(errorDescription)
                .microservice_name(microserviceName)
                .workflow_instance_id(workflowInstanceId)
                .idDocumento(idDocumento)
                .build();

        final String rawLog = StringUtility.toJSON(logDTO);

        log.info(rawLog);
        kafkaLog.info(rawLog);
    }

}
