package it.finanze.sanita.fse2.ms.gtwpublisher.client.impl;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.config.Constants;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;


/**
 * Test implemention of Eds Client.
 * 
 * @author vincenzoingenito
 */
@Slf4j
@Component
@Profile(Constants.Profile.DEV)
public class EdsMockClient implements IEdsClient {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1094030146435617088L;

	private static final String EDS_FORCE_EXCEPTION = "eds_force_exception";
	
	@Override
	public Boolean sendData(final String transactionId) {
		log.warn("ATTENZIONE : Si sta chiamando il client mockato . Assicurarsi che sia voluto");
		Boolean output = true;
		if(transactionId.trim().contains(EDS_FORCE_EXCEPTION)) {
			throw new BusinessException("Eccezione di test");
		}
		return output;
	}
	
 

}