/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   Copyright (C) 2005 - Matteo Merli - matteo.merli@gmail.com            *
 *                                                                         *
 ***************************************************************************/

/*
 * $Id$
 * 
 * $URL$
 * 
 */
package rtspproxy.lib.number;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author Matteo Merli
 */
public class UnsignedByteTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(UnsignedByteTest.class);
	}

	public void test1() {
		UnsignedByte n = new UnsignedByte(0xFF);

		assertEquals((short) 0xFF, n.shortValue());
		assertEquals(0xFF, n.intValue());
		assertEquals(0xFFL, n.longValue());
		assertEquals("0xFF", n.toHexString());
		assertEquals("255", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF }, n.getBytes()));

	}

	public void test2() {
		UnsignedByte n = new UnsignedByte(0xFFL);

		assertEquals((short) 0xFF, n.shortValue());
		assertEquals(0xFF, n.intValue());
		assertEquals(0xFFL, n.longValue());
		assertEquals("0xFF", n.toHexString());
		assertEquals("255", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF }, n.getBytes()));
	}

	public void test3() {
		UnsignedByte n = UnsignedByte.fromString("0xFF");

		assertEquals((short) 0xFF, n.shortValue());
		assertEquals(0xFF, n.intValue());
		assertEquals(0xFFL, n.longValue());
		assertEquals("0xFF", n.toHexString());
		assertEquals("255", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF }, n.getBytes()));
	}

	public void test4() {
		UnsignedByte n = UnsignedByte.fromString("255");

		assertEquals((short) 0xFF, n.shortValue());
		assertEquals(0xFF, n.intValue());
		assertEquals(0xFFL, n.longValue());
		assertEquals("0xFF", n.toHexString());
		assertEquals("255", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF }, n.getBytes()));
	}

	public void test5() {
		UnsignedByte n = UnsignedByte.fromBytes(new byte[] { (byte) 0xFF });

		assertEquals((short) 0xFF, n.shortValue());
		assertEquals(0xFF, n.intValue());
		assertEquals(0xFFL, n.longValue());
		assertEquals("0xFF", n.toHexString());
		assertEquals("255", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF }, n.getBytes()));
	}

	public void test6() {
		UnsignedByte n1 = new UnsignedByte(0);
		UnsignedByte n2 = new UnsignedByte(0xF);
		UnsignedByte n3 = new UnsignedByte(0xFFL);
		UnsignedByte n4 = UnsignedByte.fromBytes(new byte[] { (byte) 0xFF });

		assertTrue(n1.compareTo(n1) == 0);
		assertTrue(n1.compareTo(n2) < 0);
		assertTrue(n1.compareTo(n3) < 0);
		assertTrue(n1.compareTo(n4) < 0);
		assertTrue(n2.compareTo(n1) > 0);
		assertTrue(n2.compareTo(n2) == 0);
		assertTrue(n2.compareTo(n3) < 0);
		assertTrue(n2.compareTo(n4) < 0);
		assertTrue(n3.compareTo(n1) > 0);
		assertTrue(n3.compareTo(n2) > 0);
		assertTrue(n3.compareTo(n3) == 0);
		assertTrue(n3.compareTo(n4) == 0);
		assertTrue(n4.compareTo(n1) > 0);
		assertTrue(n4.compareTo(n2) > 0);
		assertTrue(n4.compareTo(n3) == 0);
		assertTrue(n4.compareTo(n4) == 0);
	}
}
