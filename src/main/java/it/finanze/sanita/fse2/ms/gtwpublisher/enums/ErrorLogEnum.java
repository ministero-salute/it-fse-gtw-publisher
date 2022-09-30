package it.finanze.sanita.fse2.ms.gtwpublisher.enums;

import lombok.Getter;

@Getter
public enum ErrorLogEnum implements ILogEnum {

	KO_EDS("KO_EDS", "Errore nella chiamata a EDS");

	private String code;

	private String description;

	private ErrorLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}

