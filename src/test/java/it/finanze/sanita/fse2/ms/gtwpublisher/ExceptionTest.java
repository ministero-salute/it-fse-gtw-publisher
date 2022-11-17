/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtwpublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;


class ExceptionTest {

	@Test
	void businessExceptionTest() {
		BusinessException exc = new BusinessException("Error"); 
		
		assertEquals(BusinessException.class, exc.getClass()); 
		assertEquals("Error", exc.getMessage()); 
		
	} 
	
	@Test
	void businessExceptionTestWithoutMsg() {
		BusinessException exc = new BusinessException(new RuntimeException()); 
		
		assertEquals(BusinessException.class, exc.getClass()); 
		
	} 

	
} 
