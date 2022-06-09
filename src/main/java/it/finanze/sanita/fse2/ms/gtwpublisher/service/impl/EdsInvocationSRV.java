package it.finanze.sanita.fse2.ms.gtwpublisher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtwpublisher.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.repository.IEdsInvocationRepo;
import it.finanze.sanita.fse2.ms.gtwpublisher.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IEdsInvocationSRV;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EdsInvocationSRV implements IEdsInvocationSRV {


	@Autowired
	private IEdsInvocationRepo edsInvocationRepo;
	
	@Autowired
	private IEdsClient edsClient;
	
	@Override
	public Boolean findAndSendToEdsByWorkflowInstanceId(final String workflowInstanceId) {
		Boolean out = false;
		try {
			IniEdsInvocationETY iniInvocationETY = edsInvocationRepo.findByWorkflowInstanceId(workflowInstanceId);
			out = true;
			edsClient.sendData(workflowInstanceId);
		} catch(Exception ex) {
			log.error("Error while running find and send to eds by workflow instance id : " , ex);
			throw new BusinessException(ex);
		}
		return out;
	}

}
