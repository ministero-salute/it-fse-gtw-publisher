/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.enums;

public enum EventStatusEnum {

	SUCCESS("SUCCESS"),
	BLOCKING_ERROR("BLOCKING_ERROR"),
	NON_BLOCKING_ERROR("NON_BLOCKING_ERROR");

	private String name;

	private EventStatusEnum(String inName) {
		name = inName;
	}

	public String getName() {
		return name;
	}

}