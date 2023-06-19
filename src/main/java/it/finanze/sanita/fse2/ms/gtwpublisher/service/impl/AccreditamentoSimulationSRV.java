/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.service.impl;

import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtwpublisher.dto.AccreditamentoSimulationDTO;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.AccreditamentoPrefixEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BlockingEdsException;
import it.finanze.sanita.fse2.ms.gtwpublisher.service.IAccreditamentoSimulationSRV;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccreditamentoSimulationSRV implements IAccreditamentoSimulationSRV {

	@Override
	public AccreditamentoSimulationDTO runSimulation(final String idDocumento) {
		AccreditamentoSimulationDTO output = null;
		AccreditamentoPrefixEnum prefixEnum = AccreditamentoPrefixEnum.getStartWith(idDocumento);
		if(prefixEnum!=null) {
			switch (prefixEnum) {
			case CRASH_EDS:
				simulateCrashEds();
				break;
			default:
				break;
			}
		}
		return output;
	}
 
	private void simulateCrashEds() {
		log.info("Simulated crash eds");
		throw new BlockingEdsException("Simulated crash eds");
	}
 
}
