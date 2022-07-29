package it.finanze.sanita.fse2.ms.gtwpublisher.dto.response;

import it.finanze.sanita.fse2.ms.gtwpublisher.dto.AbstractDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ResponseDTO extends AbstractDTO {



	/**
	 * Serial version UID 
	 */
	private static final long serialVersionUID = 5275808347369271553L;
	

	/**
	 * Trace id log.
	 */
	private String traceID;

	/**
	 * Span id log.
	 */
	private String spanID;

	/**
	 * Instantiates a new response DTO.
	 */
	public ResponseDTO() {
	}

	/**
	 * Instantiates a new response DTO.
	 *
	 * @param traceInfo the trace info
	 */
	public ResponseDTO(final LogTraceInfoDTO traceInfo) {
		traceID = traceInfo.getTraceID();
		spanID = traceInfo.getSpanID();
	} 

}