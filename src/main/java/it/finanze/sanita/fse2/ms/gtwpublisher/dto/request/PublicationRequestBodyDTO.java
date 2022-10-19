/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher.dto.request;

import it.finanze.sanita.fse2.ms.gtwpublisher.enums.PriorityTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicationRequestBodyDTO {
    private String workflowInstanceId;
    private String identificativoDoc;
    private PriorityTypeEnum priorityType;
}
