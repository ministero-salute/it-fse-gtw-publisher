package it.finanze.sanita.fse2.ms.gtwpublisher.client;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsPublicationResponseDTO;


/**
 * Interface of Eds client.
 * 
 * @author Riccardo Bonesi
 */
public interface IEdsClient extends Serializable {

    EdsPublicationResponseDTO sendData(String workflowInstanceId);

    EdsPublicationResponseDTO sendUpdateData(IndexerValueDTO valueInfo);
}
