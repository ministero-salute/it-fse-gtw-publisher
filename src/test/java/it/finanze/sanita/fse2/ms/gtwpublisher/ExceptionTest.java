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
package it.finanze.sanita.fse2.ms.gtwpublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BlockingEdsException;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.UnknownException;


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

	@Test
	void blockingEdsExceptionTest() {
		BlockingEdsException exc = new BlockingEdsException("Error"); 
		
		assertEquals(BlockingEdsException.class, exc.getClass()); 
		assertEquals("Error", exc.getMessage()); 
	} 
	
	@Test
	void blockingEdsExceptionWithMsgExcTest() {
		BlockingEdsException exc = new BlockingEdsException("Error", new RuntimeException()); 
		
		assertEquals(BlockingEdsException.class, exc.getClass()); 
		assertEquals("Error", exc.getMessage()); 
	}
	
	@Test
	void blockingEdsExceptionTestWithoutMsg() {
		BlockingEdsException exc = new BlockingEdsException(new RuntimeException()); 
		
		assertEquals(BlockingEdsException.class, exc.getClass()); 
	} 
	
	@Test
	void connectionRefusedExceptionTest() {
		String url = "testUrl";
		ConnectionRefusedException exc = new ConnectionRefusedException(url, "message"); 
		
		assertEquals(ConnectionRefusedException.class, exc.getClass());
		assertEquals(url, exc.getUrl());
	} 
	
	@Test
	void unknownExceptionTest() {
		UnknownException exc = new UnknownException("Error"); 
		
		assertEquals(UnknownException.class, exc.getClass()); 
		assertEquals("Error", exc.getMessage()); 
	} 
	
	@Test
	void unknownExceptionWithMsgExcTest() {
		UnknownException exc = new UnknownException("Error", new RuntimeException()); 
		
		assertEquals(UnknownException.class, exc.getClass()); 
		assertEquals("Error", exc.getMessage()); 
	} 
	
	@Test
	void unknownExceptionTestWithoutMsg() {
		UnknownException exc = new UnknownException(new RuntimeException()); 
		
		assertEquals(UnknownException.class, exc.getClass());
	} 
	
} 
