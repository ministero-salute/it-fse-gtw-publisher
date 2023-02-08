package it.finanze.sanita.fse2.ms.gtwpublisher.service;

import it.finanze.sanita.fse2.ms.gtwpublisher.dto.AccreditamentoSimulationDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;

public interface IAccreditamentoSimulationSRV {

	AccreditamentoSimulationDTO runSimulation(String idDocumento, byte[] pdf, EventTypeEnum eventType);
}
