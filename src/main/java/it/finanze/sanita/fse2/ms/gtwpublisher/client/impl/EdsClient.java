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
import it.finanze.sanita.fse2.ms.gtwpublisher.dto.response.EdsPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtwpublisher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * Production implemention of Eds Client.
 * 
 * @author Riccardo Bonesi
 */
@Slf4j
@Component 
public class EdsClient implements IEdsClient {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1470125906483650945L;

	@Autowired
    private transient RestTemplate restTemplate;
	
	@Autowired
	private transient MicroservicesURLCFG msUrlCFG;

	@Override
	public EdsPublicationResponseDTO sendData(final String workflowInstanceId) {

		if(workflowInstanceId == null || workflowInstanceId.isEmpty()) {
			throw new BusinessException("workflowInstanceId is null or empty");
		}

		EdsPublicationResponseDTO out = new EdsPublicationResponseDTO();
		log.info("Calling eds client - START");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<?> entity = new HttpEntity<>(workflowInstanceId, headers);

		ResponseEntity<EdsPublicationResponseDTO> response = null;
		try {
			response = restTemplate.exchange(msUrlCFG.getEdsClientHost() + msUrlCFG.getEdsClientPath() + msUrlCFG.getEdsClientPublish(), HttpMethod.POST, entity, EdsPublicationResponseDTO.class);
			out = response.getBody();
			log.info("{} status returned from Fhir mapping Client", response.getStatusCode());
		} catch (ResourceAccessException | ConnectionRefusedException cex) {
			log.error("Connect error while call eds client ep: ", cex);
			throw cex;
		} catch(Exception ex) {
			log.error("Generic error while call document reference ep: ", ex);
			throw new BusinessException("Generic error while call document reference ep: ", ex);
		}
		return out;
	}

	@Override
	public EdsPublicationResponseDTO sendUpdateData(IndexerValueDTO valueInfo) {
		if(StringUtility.isNullOrEmpty(valueInfo.getIdentificativoDocUpdate()) || StringUtility.isNullOrEmpty(valueInfo.getWorkflowInstanceId())) {
			throw new BusinessException("workflowInstanceId or identifier of document to update is null or empty");
		}

		EdsPublicationResponseDTO out = new EdsPublicationResponseDTO();
		log.info("Calling eds client to execute update of document with id: {}", valueInfo.getIdentificativoDocUpdate());
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<?> entity = new HttpEntity<>(new Gson().toJson(valueInfo), headers);

		ResponseEntity<EdsPublicationResponseDTO> response = null;
		try {
			response = restTemplate.exchange(msUrlCFG.getEdsClientHost() + "/v1/eds-replace", HttpMethod.PUT, entity, EdsPublicationResponseDTO.class);
			out = response.getBody();
			log.info("{} status returned from Fhir mapping Client", response.getStatusCode());
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
