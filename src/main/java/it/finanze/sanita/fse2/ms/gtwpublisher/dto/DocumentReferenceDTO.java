package it.finanze.sanita.fse2.ms.gtwpublisher.dto;


import it.finanze.sanita.fse2.ms.gtwpublisher.enums.ProcessorOperationEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReferenceDTO {
	private String identifier;
	private ProcessorOperationEnum operation;
	private String jsonString;
}