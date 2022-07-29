package it.finanze.sanita.fse2.ms.gtwpublisher.enums;

public enum EventSourceEnum {

	INDEXER("Indexer"),
	DISPATCHER("Dispatcher");

	private final String name;

	EventSourceEnum(String inName) {
		name = inName;
	}

	public String getName() {
		return name;
	}

}