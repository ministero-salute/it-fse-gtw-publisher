/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.client;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;


/**
 * Interface of Eds client.
 * 
 */
public interface IEdsClient extends Serializable {

    EdsPublicationResponseDTO sendPublicationData(IndexerValueDTO valueInfo, PriorityTypeEnum priorityType);

    EdsPublicationResponseDTO sendReplaceData(IndexerValueDTO valueInfo);
}
