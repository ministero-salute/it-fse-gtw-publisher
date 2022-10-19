/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.dto.response;

import it.finanze.sanita.fse2.ms.gtwpublisher.dto.AbstractDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LogTraceInfoDTO extends AbstractDTO {


	/**
	 * Serial version UID 
	 */
	private static final long serialVersionUID = -2485083840673078523L;

	
	/**
	 * Span.
	 */
	private final String spanID;
	
	/**
	 * Trace.
	 */
	private final String traceID;

}
