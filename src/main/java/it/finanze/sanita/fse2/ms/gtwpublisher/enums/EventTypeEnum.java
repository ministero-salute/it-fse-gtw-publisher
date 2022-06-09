package it.finanze.sanita.fse2.ms.gtwpublisher.enums;
 

public enum EventTypeEnum {

	SEND_TO_EDS("Send to eds"), 
	GENERIC_ERROR("Generic error from publisher");

	private String name;

	private EventTypeEnum(String inName) {
		name = inName;
	}

	public String getName() {
		return name;
	}

}