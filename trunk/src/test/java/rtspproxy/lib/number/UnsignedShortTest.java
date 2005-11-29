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
public class UnsignedShortTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(UnsignedShortTest.class);
	}

	public void test1() {
		UnsignedShort n = new UnsignedShort(0xFFFF);

		assertEquals((short) 0xFFFF, n.shortValue());
		assertEquals(0xFFFF, n.intValue());
		assertEquals(0xFFFFL, n.longValue());
		assertEquals("0xFFFF", n.toHexString());
		assertEquals("65535", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF }, n
				.getBytes()));

	}

	public void test2() {
		UnsignedShort n = new UnsignedShort(0xFFFFL);

		assertEquals((short) 0xFFFF, n.shortValue());
		assertEquals(0xFFFF, n.intValue());
		assertEquals(0xFFFFL, n.longValue());
		assertEquals("0xFFFF", n.toHexString());
		assertEquals("65535", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF }, n
				.getBytes()));

	}

	public void test3() {
		UnsignedShort n = UnsignedShort.fromString("0xFFFF");

		assertEquals((short) 0xFFFF, n.shortValue());
		assertEquals(0xFFFF, n.intValue());
		assertEquals(0xFFFFL, n.longValue());
		assertEquals("0xFFFF", n.toHexString());
		assertEquals("65535", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF }, n
				.getBytes()));
	}

	public void test4() {
		UnsignedShort n = UnsignedShort.fromString("65535");

		assertEquals((short) 0xFFFF, n.shortValue());
		assertEquals(0xFFFF, n.intValue());
		assertEquals(0xFFFFL, n.longValue());
		assertEquals("0xFFFF", n.toHexString());
		assertEquals("65535", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF }, n
				.getBytes()));
	}

	public void test5() {
		UnsignedShort n = UnsignedShort.fromBytes(new byte[] { (byte) 0xFF,
				(byte) 0xFF });

		assertEquals((short) 0xFFFF, n.shortValue());
		assertEquals(0xFFFF, n.intValue());
		assertEquals(0xFFFFL, n.longValue());
		assertEquals("0xFFFF", n.toHexString());
		assertEquals("65535", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF }, n
				.getBytes()));
	}

	public void test6() {
		UnsignedShort n1 = new UnsignedShort(0);
		UnsignedShort n2 = new UnsignedShort(0xFF);
		UnsignedShort n3 = new UnsignedShort(0xFFFFL);
		UnsignedShort n4 = UnsignedShort.fromBytes(new byte[] { (byte) 0xFF,
				(byte) 0xFF });

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
