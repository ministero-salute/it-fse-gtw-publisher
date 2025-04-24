
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.enums;


public enum EventTypeEnum {

	SEND_TO_UAR("SEND_TO_UAR"),
	SEND_TO_UDP("SEND_TO_UAR"),
	DESERIALIZE("DESERIALIZE");

	private final String name;

	EventTypeEnum(String inName) {
		name = inName;
	}

	public String getName() {
		return name;
	}

	public static EventTypeEnum getEventTypeFromDestination(String destination) {
		for (EventTypeEnum eventType : EventTypeEnum.values()) {
			if (eventType.name().equalsIgnoreCase(destination)) {
				return eventType;
			}
		}
		throw new IllegalArgumentException("No enum constant for string: " + destination);
	}

}