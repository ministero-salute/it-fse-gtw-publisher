/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.client.impl;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.PublicationRequestBodyDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * Production implemention of Eds Client.
 */
@Slf4j
@Component 
public class EdsClient implements IEdsClient {


	@Autowired
    private RestTemplate restTemplate;
	
	@Autowired
	private MicroservicesURLCFG msUrlCFG;

	@Override
	public EdsPublicationResponseDTO sendPublicationData(final IndexerValueDTO valueInfo, final PriorityTypeEnum priorityType) {

		EdsPublicationResponseDTO out = new EdsPublicationResponseDTO();
		try {
			log.debug("EDS Client - Callind EDS to send data for publishing");
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");

			PublicationRequestBodyDTO requestBody = new PublicationRequestBodyDTO();
			requestBody.setIdentificativoDoc(valueInfo.getIdDoc());
			requestBody.setWorkflowInstanceId(valueInfo.getWorkflowInstanceId());
			requestBody.setPriorityType(priorityType);

			HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);

			ResponseEntity<EdsPublicationResponseDTO> response = null;

			response = restTemplate.exchange(msUrlCFG.getEdsClientHost() + "/v1/documents", HttpMethod.POST, entity, EdsPublicationResponseDTO.class);
			out = response.getBody();
			log.debug("{} status returned from Fhir mapping Client", response.getStatusCode());
		} catch (ResourceAccessException rax) {
			log.error("",rax);
			throw rax;
		} catch(Exception ex) {
			log.error("Generic error while call eds client ep: ", ex);
			throw new BusinessException("Generic error while call eds client ep: ", ex);
		}
		return out;
	}


	@Override
	public EdsPublicationResponseDTO sendReplaceData(IndexerValueDTO valueInfo) {
		if(StringUtility.isNullOrEmpty(valueInfo.getIdDoc()) || StringUtility.isNullOrEmpty(valueInfo.getWorkflowInstanceId())) {
			throw new BusinessException("workflowInstanceId or identifier of document to update is null or empty");
		}

		EdsPublicationResponseDTO out = new EdsPublicationResponseDTO();
		log.debug("EDS Client - Calling eds client to execute update of document with id: {}", valueInfo.getIdDoc());
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<?> entity = new HttpEntity<>(new Gson().toJson(valueInfo), headers);

		ResponseEntity<EdsPublicationResponseDTO> response = null;
		try {
			response = restTemplate.exchange(msUrlCFG.getEdsClientHost() + "/v1/documents/" + valueInfo.getIdDoc(), HttpMethod.PUT, entity, EdsPublicationResponseDTO.class);
			out = response.getBody();
			log.debug("{} status returned from Fhir mapping Client", response.getStatusCode());
		} catch (ResourceAccessException | ConnectionRefusedException cex) {
			log.error("Connection error while calling eds client endpoint to execute replace of document: ", cex);
			throw cex;
		} catch(Exception ex) {
			log.error("Generic error while calling eds client endpoint to execute replace of document: ", ex);
			throw new BusinessException("Generic error while calling eds client endpoint to execute replace of document: ", ex);
		}
		return out;
	}

}
