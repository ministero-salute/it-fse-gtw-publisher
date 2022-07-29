package it.finanze.sanita.fse2.ms.gtwpublisher.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EdsPublicationResponseDTO {

	private Boolean esito;

	private String errorMessage;
}
