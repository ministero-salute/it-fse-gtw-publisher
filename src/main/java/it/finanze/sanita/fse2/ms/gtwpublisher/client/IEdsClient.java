/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.client;

import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsTraceResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;


/**
 * Interface of Eds client.
 */
public interface IEdsClient {

    EdsTraceResponseDTO sendPublicationData(IndexerValueDTO valueInfo, PriorityTypeEnum priorityType);

    EdsTraceResponseDTO sendReplaceData(IndexerValueDTO valueInfo);
}
