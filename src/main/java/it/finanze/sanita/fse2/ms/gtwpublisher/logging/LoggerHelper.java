/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
            return;
        }

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

        kafkaLog.info(StringUtility.toJSON(logDTO));
    }
}
