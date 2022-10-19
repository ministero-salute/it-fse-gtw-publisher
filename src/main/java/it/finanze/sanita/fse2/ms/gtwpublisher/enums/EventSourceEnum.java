/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.enums;

import lombok.Getter;

public enum EventSourceEnum {

	INDEXER("Indexer", "Indexer"),
	DISPATCHER("Dispatcher", "TSFeeding");

	@Getter
	private final String name;

	@Getter
	private final String description;

	EventSourceEnum(String inName, String inDescription) {
		name = inName;
		description = inDescription;
	}

}