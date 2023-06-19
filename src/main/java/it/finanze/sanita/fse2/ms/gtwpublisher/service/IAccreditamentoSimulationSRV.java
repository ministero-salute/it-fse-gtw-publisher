/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.service;

import it.finanze.sanita.fse2.ms.gtwpublisher.dto.AccreditamentoSimulationDTO;

public interface IAccreditamentoSimulationSRV {

	AccreditamentoSimulationDTO runSimulation(String idDocumento);
}
