/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.enums;
 

public enum EventTypeEnum {

	SEND_TO_EDS("SEND_TO_EDS"),
	DESERIALIZE("DESERIALIZE");

	private final String name;

	EventTypeEnum(String inName) {
		name = inName;
	}

	public String getName() {
		return name;
	}

}