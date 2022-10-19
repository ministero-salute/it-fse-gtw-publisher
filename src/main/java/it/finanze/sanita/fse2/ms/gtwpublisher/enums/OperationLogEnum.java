/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.enums;

import lombok.Getter;

@Getter
public enum OperationLogEnum implements ILogEnum {

	SEND_EDS("TRAS-CDA2", "Invio a EDS");

	private String code;

	private String description;

	private OperationLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}

