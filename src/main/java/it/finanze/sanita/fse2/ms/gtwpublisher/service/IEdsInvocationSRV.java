package it.finanze.sanita.fse2.ms.gtwpublisher.service;

public interface IEdsInvocationSRV {

	Boolean findAndSendToEdsByWorkflowInstanceId(String workflowInstanceId);
}
