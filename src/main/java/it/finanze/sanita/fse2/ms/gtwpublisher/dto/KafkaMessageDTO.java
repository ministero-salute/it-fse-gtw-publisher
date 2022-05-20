package  it.finanze.sanita.fse2.ms.gtwpublisher.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Content of a Kafka message
 */
@Getter
@Builder
public class KafkaMessageDTO extends AbstractDTO {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2144344497297675698L;

    /**
     * Message.
     */
    private String message;

         
}
