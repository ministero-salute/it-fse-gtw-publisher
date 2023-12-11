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

import it.finanze.sanita.fse2.ms.gtwpublisher.client.IFhirPublisherClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.request.PublicationRequestBodyDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.FhirPublisherResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * Production implemention of Eds Client.
 */
@Slf4j
@Component 
public class FhirPublisherClient implements IFhirPublisherClient {

	@Autowired
    private RestTemplate restTemplate;
	
	@Autowired
	private MicroservicesURLCFG msUrlCFG;

	@Override
	public FhirPublisherResponseDTO sendPublicationData(final IndexerValueDTO valueInfo, final PriorityTypeEnum priorityType) {

		FhirPublisherResponseDTO out = new FhirPublisherResponseDTO();
		try {
			log.debug("EDS Client - Callind EDS to send data for publishing");
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");

			PublicationRequestBodyDTO requestBody = new PublicationRequestBodyDTO();
			requestBody.setIdentificativoDoc(valueInfo.getIdDoc());
			requestBody.setWorkflowInstanceId(valueInfo.getWorkflowInstanceId());
			requestBody.setPriorityType(priorityType);

			HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);

			ResponseEntity<FhirPublisherResponseDTO> response;

			response = restTemplate.exchange(msUrlCFG.getEdsClientHost() + "/v1/documents", HttpMethod.POST, entity, FhirPublisherResponseDTO.class);
			out = response.getBody();
			log.debug("{} status returned from Fhir mapping Client", response.getStatusCode());
		} catch (ResourceAccessException rax) {
			log.error("",rax);
			throw rax;
		} catch(Exception ex) {
			log.error("Generic error while call eds client ep: ", ex);
			throw ex;
		}
		return out;
	}


	@Override
	public FhirPublisherResponseDTO sendReplaceData(IndexerValueDTO valueInfo) {
		if(StringUtility.isNullOrEmpty(valueInfo.getIdDoc()) || StringUtility.isNullOrEmpty(valueInfo.getWorkflowInstanceId())) {
			throw new BusinessException("workflowInstanceId or identifier of document to update is null or empty");
		}

		FhirPublisherResponseDTO out = new FhirPublisherResponseDTO();
		log.debug("EDS Client - Calling eds client to execute update of document with id: {}", valueInfo.getIdDoc());
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<?> entity = new HttpEntity<>(new Gson().toJson(valueInfo), headers);

		ResponseEntity<FhirPublisherResponseDTO> response;
		try {
			response = restTemplate.exchange(msUrlCFG.getEdsClientHost() + "/v1/documents/" + valueInfo.getIdDoc(), HttpMethod.PUT, entity, FhirPublisherResponseDTO.class);
			out = response.getBody();
			log.debug("{} status returned from Fhir mapping Client", response.getStatusCode());
		} catch (ResourceAccessException cex) {
			log.error("Connection error while calling eds client endpoint to execute replace of document: ", cex);
			throw cex;
		} catch(Exception ex) {
			log.error("Generic error while calling eds client endpoint to execute replace of document: ", ex);
			throw new BusinessException("Generic error while calling eds client endpoint to execute replace of document: ", ex);
		}
		return out;
	}

}
