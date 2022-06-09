package it.finanze.sanita.fse2.ms.gtwpublisher.dto;

import java.util.Date;

import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtwpublisher.enums.EventTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaStatusManagerDTO extends AbstractDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 7080680277816570116L;
	
	private EventTypeEnum eventType;
	
	private Date eventDate;
	
	private EventStatusEnum eventStatus;
	
	private String exception;
}
 